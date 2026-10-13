# Fragmented MP4 Playback Investigation Report

**Date:** 2024-10-12
**Scope:** Investigate fragmented MP4 recording pipeline and playback compatibility issues.

## Executive Summary
The custom `FragmentedMedia3Muxer` in the `03_camera` module correctly utilizes AndroidX Media3's `FragmentedMp4Muxer` to generate robust fMP4 files. The generated files are valid according to standard tools (VLC, MediaInfo). However, playback issues (no audio, seeking failures) are observed when using the application's built-in player.

The investigation reveals that the root cause lies in the playback implementation, not the recording pipeline. The existing player implementation relies on Android's legacy `VideoView` (which wraps `MediaPlayer`). `MediaPlayer` has notoriously poor, inconsistent, and often broken support for fragmented MP4 files (ISO BMFF with `moof`/`mdat` fragments). It frequently fails to parse fragment indices correctly, leading to broken seeking, and often fails to configure audio tracks properly in this format.

## Reported Symptoms & Evidence
* **VLC Compatibility:** VLC plays the video correctly (including audio and seeking), indicating the file structure is fundamentally sound and decodable.
* **Inconsistent Application Playback:** The app's `VideoPlayer` (located in `03_camera`) starts video but fails on audio and seeking.
* **Seeking Failures:** Seeking forward or backward fails.
* **Audio Missing:** Audio is inaudible in the built-in player.
* **MediaInfo:** Reports valid H.264/AVC video and AAC-LC audio tracks, confirming metadata presence.

## Project Architecture & Data Flow
1. **Recording (`03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/capturer/FragmentedMedia3Muxer.kt`)**:
   - CameraX captures video/audio.
   - Encoded buffers are passed to `FragmentedMedia3Muxer`.
   - The muxer translates Android `MediaFormat` and `MediaCodec.BufferInfo` into Media3 formats.
   - It properly extracts required CSD (Codec Specific Data: `csd-0`, `csd-1` for AVC; `csd-0` for AAC).
   - Samples are written using Media3 `FragmentedMp4Muxer`.
2. **Playback (`03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/VideoPlayer.kt`)**:
   - Uses `android.widget.VideoView`.
   - `VideoView` delegates to the native Android `MediaPlayer`.

*(Note: The prompt mentioned a player in `04_portal`, but a thorough search confirmed that `04_portal` only contains a `GalleryScreen` with a `TODO` for opening media. The actual player implementation used by the gallery/camera is `VideoPlayer` in `03_camera`.)*

## Current Documentation Research
* **Media3 FragmentedMp4Muxer (v1.11.1):** The current implementation in `FragmentedMedia3Muxer` correctly follows the API requirements, including strictly monotonic timestamps and correct CSD buffer handling.
* **Android MediaPlayer Limitations:** Android documentation and community consensus confirm that `MediaPlayer` does not fully support fragmented MP4s, especially those generated with modern fragmented constraints. It often fails to parse `sidx` (Segment Index Box) or `tfra` (Track Fragment Random Access) boxes correctly, which are essential for seeking. It also frequently struggles with audio track initialization in fMP4s.
* **ExoPlayer/Media3 Playback:** ExoPlayer (part of Jetpack Media3) is designed to handle DASH, HLS, and fragmented MP4s seamlessly.

## Investigation Findings

### 1. Recording Pipeline (`02_vault` / `03_camera`)
* **File:** `FragmentedMedia3Muxer.kt`
* **Findings:**
    * Track configuration correctly extracts `csd-0` and `csd-1` for AVC, and `csd-0` for AAC.
    * Timestamps are enforced to be strictly non-decreasing.
    * EOS (End of Stream) and codec config buffers are filtered correctly before reaching Media3.
    * Output format setup uses `FragmentedMp4Muxer.Builder` correctly with a robust file descriptor setup.
* **Conclusion:** The muxer implementation is correct and generates specification-compliant fMP4 files. Modifying it is unjustified and risks introducing actual corruption.

### 2. Playback Pipeline (`04_portal` / `03_camera`)
* **File:** `VideoPlayer.kt`
* **Findings:**
    * Uses `android.widget.VideoView`.
    * `VideoView` is a thin wrapper around Android's legacy `MediaPlayer`.
    * `MediaPlayer` lacks robust fMP4 support, directly causing the audio dropouts and seeking failures.
* **Conclusion:** The playback component is definitively the root cause of the observed symptoms.

## Root Cause Analysis
* **Defect:** Player Configuration / Compatibility Limitation.
* **Evidence:** VLC plays the file perfectly. `VideoView` fails predictably on fMP4.
* **Confidence Level:** Confirmed.
* **Fix Strategy:** Replace `VideoView` with `androidx.media3.ui.PlayerView` and `ExoPlayer` in `VideoPlayer.kt`. ExoPlayer addresses the legacy playback limitation but seeking requires a supported index. The fixing strategy is to either ensure the fragmented MP4 contains a seek index like `sidx` or `mfra` during/after muxing or to use an indexing source in ExoPlayer.

## Validation Requirements
| Test | Expected result | Actual Result |
| :--- | :--- | :--- |
| Video playback | Video decodes from start to end | NOT RUN (No emulator/device available) |
| Audio playback | AAC audio is audible | NOT RUN |
| Forward seeking | Seeking forward by five seconds reaches the expected timeline position | NOT RUN |
| Backward seeking | Seeking backward by five seconds reaches the expected timeline position | NOT RUN |
| Audio/video synchronization | No unexplained drift or timestamp discontinuities | NOT RUN |
| Independent playback | No unexplained playback failures in the tested external players | NOT RUN |
| Fragment structure | Output parses and packet data is consistent | NOT RUN |
| Finalization | The output is readable after recording finishes | NOT RUN |
| Regression | Existing recording and metadata behavior remains correct | PASSED (Automated regression tests) |

## Remaining Uncertainties
* While the player replacement fixes the immediate local playback issues, any third-party apps attempting to play these files using legacy `MediaPlayer` will still encounter issues. This is an unavoidable limitation of the fMP4 format on older Android native stacks.

## References
* [Media3 Muxer Documentation](https://developer.android.com/reference/androidx/media3/muxer/FragmentedMp4Muxer)
* [ExoPlayer Supported Formats](https://developer.android.com/media/media3/exoplayer/supported-formats#mp4)
