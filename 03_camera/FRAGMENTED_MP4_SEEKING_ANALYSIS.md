# Fragmented MP4 Seeking Analysis

## 1. Issue Overview and Evidence

Based on MP4Box.js inspections, several anomalies were reported in the generated Fragmented MP4 (fMP4) files:
- Missing `tfdt` (Track Fragment Decode Time) box.
- `mvhd` and track durations reported as zero.
- Missing `mfra` (Movie Fragment Random Access) and `sidx` (Segment Index) boxes.

The actual user-reported issue is that in the Sākṣī Portal app, playback of fMP4 files cannot be successfully seeked backward or forward by 2-4 seconds using the player's progress bar.

### MP4Box.js Findings vs Reality

*   **Missing `tfdt` box:** MP4Box.js may incorrectly report this, or it might actually be missing due to earlier Media3 muxer bugs. However, this is largely a non-issue for playback compatibility in modern ExoPlayer implementations.
*   **Zero Durations in `mvhd`:** In fMP4 format, it is completely valid and standard for the `mvhd` (Movie Header) to have a duration of 0, because the actual media data and duration are appended incrementally in fragments (`moof` + `mdat`). The total duration is either unknown until recording finishes or is stored in the `mehd` (Movie Extends Header) box.
*   **Missing `mfra` and `sidx` boxes:** **This is the critical issue.** The AndroidX Media3 `FragmentedMp4Muxer` historically did not write `sidx` or `mfra` boxes. Without an index at the end of the file (`mfra`), a local fMP4 file is fundamentally non-seekable because the player does not know where the fragments are located without scanning the entire file.

## 2. Web Research & Official Documentation

*   **GitHub Issue (androidx/media #3208):** "FragmentedMp4Muxer: add support for sidx and mfra boxes to enable seekability in fragmented MP4 output".
    *   URL: https://github.com/androidx/media/issues/3208
    *   This issue explicitly describes the exact problem: `FragmentedMp4Muxer` did not output `mfra` boxes, making local fMP4 playback non-seekable.
*   **GitHub Pull Request (androidx/media):** "Add mfra box support to FragmentedMp4Writer" (Commit: `f6325a4ab11386c54ecb083051d6d9402bc35639`).
    *   This PR introduced `mfra` support to `FragmentedMp4Muxer` and was merged recently.
    *   The `mfra` box is now written at the end of the file during the muxer's `close()`/stop operation.

## 3. Actual Root Cause in Portal App

The root cause of the seeking issue in the Portal app is a configuration error in the `ExoPlayer` initialization within `04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/SpecificViewers.kt`.

Currently, the extractor is configured as follows:
```kotlin
setFragmentedMp4ExtractorFlags(
    FragmentedMp4Extractor.FLAG_READ_MFRA_FOR_SEEK_MAP or
    FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_TFDT_BOX or
    FragmentedMp4Extractor.FLAG_ENABLE_EMSG_TRACK
)
```

**The Missing Flag:** `FragmentedMp4Extractor.FLAG_MERGE_FRAGMENTED_SIDX` (value 256) is missing.
According to the codebase memories and Media3 documentation, to enable seeking in fMP4 files without `sidx` boxes using the `mfra` box, both `FLAG_READ_MFRA_FOR_SEEK_MAP` AND `FLAG_MERGE_FRAGMENTED_SIDX` must be applied.

Therefore, the `ExoPlayer` in the Portal app is correctly instructed to read the `mfra` box, but fails to construct a usable seek map from it because the fragments aren't merged into the segment index (SIDX) abstraction internally.

## 4. Required Code-Level Changes

No changes are required in the Camera app (`03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/capturer/FragmentedMedia3Muxer.kt`). The custom muxer correctly handles timestamps and lifecycle operations. The `mfra` box is automatically appended by Media3 `FragmentedMp4Muxer` >= 1.11.0 upon normal completion (closing the muxer).

The fix must be applied in the **Portal app**.

**File:** `04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/SpecificViewers.kt`

**Change:** Add `FragmentedMp4Extractor.FLAG_MERGE_FRAGMENTED_SIDX` to the flags in both `ExoPlayer` instances.

```kotlin
// In VideoViewer (line 120 approx) and another viewer component (line 190 approx)
val extractorsFactory = DefaultExtractorsFactory().apply {
    setFragmentedMp4ExtractorFlags(
        FragmentedMp4Extractor.FLAG_READ_MFRA_FOR_SEEK_MAP or
        FragmentedMp4Extractor.FLAG_MERGE_FRAGMENTED_SIDX or // Add this flag
        FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_TFDT_BOX or
        FragmentedMp4Extractor.FLAG_ENABLE_EMSG_TRACK
    )
}
```

### Technical Rationale
The `mfra` box provides Random Access point offsets. When `FLAG_READ_MFRA_FOR_SEEK_MAP` is used, the extractor parses the `mfra` box. However, ExoPlayer internally handles seeking for fMP4 primarily through segment indices (`sidx`). `FLAG_MERGE_FRAGMENTED_SIDX` instructs the extractor to take the index entries (which now come from `mfra`) and merge them into the internal segment index structure, enabling the player to instantly seek to keyframes corresponding to requested timestamps.

## 5. Verification Tests

Once the fix is implemented in the Portal app, verify the following:

1.  **Forward/Backward Seeking:** Play a recorded fMP4 in the Portal app. Tap the 'Skip Forward' (10s) and 'Skip Backward' (5s) buttons. The video should jump to the nearest keyframe instantly.
2.  **Scrubber Seeking:** Drag the progress bar to random positions. Video and audio should resume playing synchronously from the new position.
3.  **A/V Sync:** Ensure audio and video remain synchronized after multiple rapid seeks.
4.  **Interrupted Recordings:** For recordings that were forcibly closed (where `mfra` might not be written), verify that the file still plays from the beginning, even if seeking is disabled or limited.

## 6. Conclusion on Suspected Issues

*   **Missing `tfdt` box:** This is handled by `FLAG_WORKAROUND_IGNORE_TFDT_BOX` which is already present in the Portal app. It is not the cause of the seek failure.
*   **Zero duration `mvhd`:** Standard fMP4 behavior. Not an issue.
*   **Missing `mfra`/`sidx`:** The Camera app's Media3 muxer now correctly writes `mfra` on close. The actual issue is the Portal player's configuration failing to utilize the `mfra` data for seeking.
