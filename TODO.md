# TASK: Implement Crash-Safe Fragmented MP4 (fMP4) Video Recording with Selector UI in 03_camera

## 1. Objective & Overview
We need to add **Fragmented MP4 (fMP4)** recording support to the `03_camera` module in `Sakshi-All`.
- **Default Format:** `fMP4` (Marked as "Recommended" / "Crash-Safe").
- **Alternative Option:** Standard `MP4` (Legacy / Maximum compatibility).
- **Settings UI (`MoreSettings`):** Add a format selector setting under the Video section. It must **NOT** be a toggle switch or text input; it must match existing multi-choice settings in the app where clicking the row / right-side value displays a single-choice dialog or popup list of options to choose from.

---

## 2. Strict Ground Rules & Constraints
1. **Follow `AGENT.md` Rules:** Strictly adhere to all architectural, coding, and safety guidelines outlined in `AGENT.md`.
2. **Minimal & Clean Diffs:** Do not modify unrelated files, refactor existing working features, or reformat untouched files. Maintain full backward compatibility for photos, vault storage/encryption, orientation, and flash controls.
3. **Web Search Mandate (No Guesswork):**
   - Your internal training data is outdated.
   - You **MUST search the web** for the latest official Android and AndroidX Media3 documentation (specifically `androidx.media3.muxer.FragmentedMp4Muxer` and CameraX video pipeline integrations).
   - Use the **latest stable versions** for any new dependencies (e.g., `androidx.media3:media3-muxer`).
4. **Environment / VM Note:** If any `mavenPublish` or publication plugin error occurs during build/check tasks, **ignore it completely**. It is an environmental limitation of the local VM. Do not attempt to modify publication scripts.
5. **Documentation:** Update relevant code comments, enums, and project documentation/README to reflect the new fMP4 default and recording container options.

---

## 3. Technical Implementation Details

### A. Configuration & Storage (`CamConfig.kt`)
1. Add configuration keys and constants for the video container format:
   - `KEY_VIDEO_CONTAINER_FORMAT` (or project standard naming convention).
   - Define format options:
     - `FORMAT_FMP4`: Display label `"fMP4 (Crash-Safe, Recommended)"` (Value: `0` or `"fmp4"`).
     - `FORMAT_MP4`: Display label `"MP4 (Standard Compatibility)"` (Value: `1` or `"mp4"`).
2. Ensure `FORMAT_FMP4` is returned as the **default** when no user preference has been saved yet.

### B. Settings UI Implementation (`MoreSettings.kt` / `MoreSettingsSecure.kt`)
1. Inspect how other multi-choice preferences (such as Resolution, Quality, or FPS) are implemented in `MoreSettings`:
   - The UI item must consist of a title (e.g., *"Video Container Format"*), a subtitle/description, and the current selected value displayed on the right side.
   - It must **NOT** be a Switch/Checkbox toggle and **NOT** a manual text input.
2. Interaction Behavior:
   - Tapping the row (or the right-side value) must trigger a **Single-Choice Selection Dialog** (e.g., `MaterialAlertDialogBuilder` / `AlertDialog` with radio buttons or a popup menu), listing:
     1. `fMP4 (Crash-Safe, Recommended)`
     2. `MP4 (Standard Compatibility)`
   - The currently active format must be pre-selected in the list.
   - Upon selecting an item:
     - Save the new choice into `CamConfig` / `SharedPreferences`.
     - Immediately update the text label on the right side of the settings row.
     - Dismiss the dialog.

### C. Core Recording Engine (`VideoCapturer.kt` & Pipeline)
*Context:* CameraX's built-in `Recorder` (`androidx.camera.video.Recorder`) relies on Android's native `MediaMuxer`, which only writes standard MP4 with a trailing `moov` atom.
1. **Normal MP4 Mode:**
   - Continue using the standard, battle-tested CameraX `VideoCapture` / `Recorder` flow.
2. **fMP4 Mode:**
   - Integrate `androidx.media3.muxer.FragmentedMp4Muxer`.
   - Setup the video encoding pipeline (via CameraX video stream / `MediaCodec`) to feed samples into `FragmentedMp4Muxer`.
   - Ensure fragments are written periodically (every 1–2 seconds / per keyframe GOP) with valid `moof` + `mdat` boxes.
   - Ensure PTS (presentation timestamp) monotonic continuity and correct A/V synchronization.
   - Verify that in the event of an abrupt stop (app crash, kill, battery removal), all media recorded up to the last written fragment remains 100% playable.
3. **Abstraction:**
   - Cleanly encapsulate the muxing backend inside `VideoCapturer.kt` so that UI activities (`VideoCaptureActivity`, `VideoOnlyActivity`) remain agnostic to whether `fMP4` or `MP4` is running under the hood.

---

## 4. Verification & Quality Checklist
- [ ] Build compiles cleanly without Gradle errors.
- [ ] Fresh installs default to `fMP4 (Crash-Safe, Recommended)`.
- [ ] In `MoreSettings`, tapping the Video Format row opens the single-choice dialog displaying both options.
- [ ] Selecting an option persists across app restarts and updates the UI label immediately.
- [ ] Recording in `fMP4` mode writes valid fragmented MP4 files that play back seamlessly in ExoPlayer / VLC.
- [ ] Abruptly killing the recording process in `fMP4` mode leaves the recorded file playable up to the last fragment.
- [ ] Recording in standard `MP4` mode functions normally with standard `moov` header finalization.
- [ ] Sakshi Vault encryption/saving pipeline seamlessly accepts and encrypts both formats.
