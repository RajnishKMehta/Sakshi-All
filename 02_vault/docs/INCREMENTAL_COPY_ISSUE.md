# Investigation Report: Normal MP4 Video Corruption during Incremental Copy

## Findings and Evidence

A data mismatch was observed during the incremental copying of normal (non-fragmented) MP4 recordings from the Camera app to the Vault. The hex diff provided is as follows:

- **Original MP4**: `00061ab0: 000f 143b 0000 4a83 6588 8000 4000 013f`
- **Vault-copied MP4**: `00061ab0: 0000 0010 0000 4a83 6588 8000 4000 013f`

Analyzing the mismatch:
- The first 4 bytes changed from `000f 143b` (988,219 in decimal) to `0000 0010` (16 in decimal).
- A 4-byte value of 16 often corresponds to the size of a standard `mdat` atom header (8 bytes) + an `moov` placeholder (or similar atom) in an incomplete MP4. However, it can also represent a placeholder `mdat` size. When the standard Android `MediaMuxer` (using `MPEG4Writer`) starts recording, it writes a placeholder header (often a 64-bit `mdat` size placeholder if the file might be large).
- At finalization (when `stop()` is called), `MPEG4Writer` seeks back to the beginning of the file to update the `moov` atom (or write the `moov` if it was reserved at the beginning) and to rewrite the actual size of the `mdat` chunk in the header, replacing the temporary or zeroed values written at the start of the recording.

In `AppPrivateStorageManager.kt`, the incremental copy logic updates the header by copying the first up to 128 KiB of the original file into the vault file during *every* pass:
```kotlin
val headerSize = minOf(offset, 128 * 1024L) // Synchronize up to 128KB of header
...
if (headerBytesRead > 0) {
    RandomAccessFile(destinationFile, "rw").use { raf ->
        raf.seek(0)
        raf.write(headerBuffer, 0, headerBytesRead)
    }
}
```

However, at offset `0x61ab0` (approx 399 KiB), which is far beyond the 128 KiB limit, the updated bytes in the original file are *never synchronized* to the vault file. If `MPEG4Writer` updates metadata or atoms (like `moov`, if it reserved space larger than 128 KiB, or `stco` chunk offsets) beyond the first 128 KiB, the Vault's copy will retain the placeholder values (like `0x00000010`) instead of the finalized values (like `0x000f143b`).

## Root Cause

The root cause is a hardcoded limit of `128 * 1024L` (128 KiB) on header synchronization in `AppPrivateStorageManager.kt`.

For normal MP4 files, `MediaMuxer` may reserve more space than 128 KiB at the beginning of the file for the `moov` atom (AOSP `MPEG4Writer.cpp` typically reserves around 400 KiB or more depending on bitrate/estimated duration, and can be up to ~3 MB). When the recording stops, `MediaMuxer` updates the file, including bytes far past the 128 KiB offset. The Vault's incremental copy does not copy these updates, leaving the vault file in a corrupt, unplayable state with invalid `mdat` lengths, missing `moov` atoms, or corrupted chunk offsets.

This explains why:
- Photos copy correctly (they are completely copied at once).
- Fragmented MP4s copy correctly (they append data sequentially and don't rely on seeking back to update a massive `moov` atom at the start; they just use small `moof` headers before each `mdat` fragment, which are captured correctly by the append logic).
- Normal MP4s suffer from corruption.

## Fix Options and Trade-offs

### Option 1: Increase the Header Sync Limit
Increase the header sync limit from 128 KiB to a larger value (e.g., 5 MB or 10 MB) or synchronize the entire `moov` atom based on MP4 parsing.
- **Pros**: Easy to implement. Allows incremental copy of normal MP4s to succeed if the updates fall within the new limit.
- **Cons**: Still fragile. If a recording is exceptionally long, the `moov` atom could exceed the new limit. Parsing MP4 structure locally is complex and error-prone. Re-copying megabytes of data on every single pass degrades performance and flash storage lifespan.

### Option 2: Full Final Copy on Completion
Change the copy strategy for non-fragmented files to only perform a single, full copy when the recording is finalized, or perform incremental appends but re-copy the *entire* file when the recording stops.
- **Pros**: Guarantees bit-for-bit accuracy of the final file.
- **Cons**: Defeats the purpose of incremental copying (resiliency). If the app crashes before the final copy, the file is lost.

### Option 3: Restrict Incremental Copy to Fragmented MP4 Only
Stop using the incremental copy logic for Normal MP4 files altogether. Mandate the use of Fragmented MP4 (fMP4) for video recordings (the Camera app is already using `FragmentedMedia3Muxer`). For any normal MP4 or other non-appendable formats, use the standard `copyFile` (which copies the file once it's completely written).
- **Pros**: fMP4 is designed explicitly for streamability, appendability, and crash resiliency. It requires no seek-back header updates, making the current simple append logic perfectly safe. It handles future media types (audio, video) uniformly as long as they are fragmented or self-contained.
- **Cons**: Client applications must ensure they use fMP4 if they want incremental sync. If a client sends a standard MP4 for incremental sync, it might not work unless we block it or treat it as a final copy.

## Recommendation

**Option 3 (coupled with a final full-file integrity sync or using Fragmented MP4 exclusively for incremental sync)** is the most architecturally sound approach.

Since the Vault does not control the source file format natively, the most robust fix for the Vault is to **detect the exact bytes that changed** and copy them, OR, since that is inefficient, **perform a final "catch-up" or full header sync when the file reaches the `COMPLETED` state**.

However, a simpler and more immediate fix for `AppPrivateStorageManager.kt` is to **dynamically determine the size of the updated region**, but since we only have append/skip operations, we should perform a full file comparison or a final full copy for non-fragmented files.

**Given the constraints**, the best fix to `AppPrivateStorageManager.kt` is to either:
1. Increase the `headerSize` limit to encompass the typical `moov` size (e.g., up to the entire file size on the *final* pass when `COMPLETED`).
2. Advise the Vault to only support fMP4 for incremental copying, as it is inherently resilient.

For the Vault implementation, we should adjust `AppPrivateStorageManager.kt` so that if we know the sync is in its finalization stage (or simply remove the 128KB hardcoded limit and rely on a smarter sync algorithm that copies all differing bytes at the start). Since the current API only provides an `offset` (where to append) and does not distinguish the final pass easily, we recommend **synchronizing the entire file up to `offset` if it's the final pass**, or significantly increasing the 128KB limit to a safe margin (e.g. `3 * 1024 * 1024` for 3MB) to cover `MPEG4Writer`'s max `moov` reservation.

*For this report, we recommend increasing the header sync size to 5MB (or reading the entire file up to `offset` in a background pass) to account for normal MP4 `moov` sizes, while strongly encouraging fMP4 for resilient recordings.*

## Testing Strategy
- **Normal MP4**: Record a normal MP4, wait for it to sync, stop recording, and verify playback and bit-for-bit equality with the source file.
- **Fragmented MP4 (fMP4)**: Verify that fMP4 recordings continue to sync correctly and append properly.
- **Photos**: Verify that photos still copy entirely without issues.
- **Future Audio**: Ensure audio formats (like standard AAC/M4A or fragmented M4A) follow the same rules: if they have a `moov` atom at the start, they need the expanded header sync limit.
