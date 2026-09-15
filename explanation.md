# Explanation of Changing `copyMediaIncremental` to `copyFile` in `SyncScheduler.kt`

Replacing `copyMediaIncremental` with `copyFile` during the final sync pass in `SyncScheduler.kt` addresses a critical issue related to how video files (specifically MP4s) are finalized and processed.

## What will happen if we make this change?

1. **Proper Finalization of MP4 Files:**
   When you change the final sync pass to use `copyFile`, the system will perform a complete read and write of the video file instead of merely appending remaining bytes. This ensures that the fully finalized video file (including its headers and `moov` atoms) is transferred to the Vault.

2. **Prevention of Thumbnail Extraction Errors:**
   Because the final MP4 structure is intact in the Vault, libraries like AndroidX Media3 (used for thumbnail extraction) will be able to parse the video properly. This prevents `IndexOutOfBoundsException` or memory issues that occur when Media3 attempts to read a corrupt or incomplete video file.

3. **Accurate File Size and Content:**
   The `lastCopiedOffset` logic in incremental syncing might miss changes made to the beginning of the file (such as updating headers). A full `copyFile` guarantees that the Vault has a 1-to-1 exact copy of the source video file as it was finalized by the Camera.

## Differences between `copyMediaIncremental` and `copyFile`

| Feature | `copyMediaIncremental` | `copyFile` |
|---------|------------------------|------------|
| **Behavior** | Reads the source file starting from `lastCopiedOffset` and appends new bytes to the end of the existing file in the Vault. | Reads the source file entirely from the beginning (byte 0) to the end and overwrites/creates the file in the Vault. |
| **Use Case** | Ideal for **active synchronization** while a video or audio file is still being recorded. It continuously transfers new chunks to save time and bandwidth. | Ideal for the **final sync pass** (or single photo copies). It ensures the final, complete state of the file is saved. |
| **Handling of MP4 Metadata** | **Fails** to capture final metadata. MP4 files often write their critical headers (`moov` atoms) at the very end of recording, sometimes updating data at the beginning of the file. Appending only new bytes misses these structural changes. | **Succeeds** in capturing metadata. By copying the entire file after the recording has stopped, it guarantees all headers and atoms are perfectly preserved. |
| **Performance** | Faster during continuous recording as it only copies small new chunks of data. | Slower and more resource-intensive, as it re-copies the entire file. However, this is necessary once at the end of recording to ensure file integrity. |

## Conclusion
By making this change in `stopSync()`, you are ensuring that the Vault correctly finalizes video files, preserving their structure and preventing downstream crashes when trying to generate thumbnails or play the video.
