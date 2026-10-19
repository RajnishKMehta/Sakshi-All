# Fragmented MP4 Seeking Issue Investigation and Fix

## Problem Description
In the Sakshi Portal app, Fragmented MP4 (fMP4) videos play correctly, but seeking using the progress slider, skip backward (5 seconds), and skip forward (10 seconds) controls does not work.

## Investigation & Evidence
1. **Media3 `FragmentedMp4Extractor` Flags**: The Portal app relies on Media3 for playback. In `04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/SpecificViewers.kt`, the `FragmentedMp4Extractor` is configured with:
    ```kotlin
    setFragmentedMp4ExtractorFlags(
        androidx.media3.extractor.mp4.FragmentedMp4Extractor.FLAG_READ_MFRA_FOR_SEEK_MAP or
        androidx.media3.extractor.mp4.FragmentedMp4Extractor.FLAG_WORKAROUND_IGNORE_TFDT_BOX or
        androidx.media3.extractor.mp4.FragmentedMp4Extractor.FLAG_ENABLE_EMSG_TRACK
    )
    ```
    According to memory context and the official Media3 GitHub issues (e.g., #3088), starting with Media3 1.11.0+, supporting fMP4 seeking using the `mfra` box (when `sidx` boxes are absent or not usable) requires applying both `FLAG_READ_MFRA_FOR_SEEK_MAP` and `FLAG_MERGE_FRAGMENTED_SIDX` flags. The `FLAG_MERGE_FRAGMENTED_SIDX` flag is missing from the current configuration.

2. **Compose Player Control Visibility**: The user reported that "skip backward (5 seconds), and skip forward (10 seconds) controls do not work."
    Inspecting the UI implementation in `SpecificViewers.kt`:
    ```kotlin
    centerControls = { player, visible ->
        androidx.media3.ui.compose.material3.PlayerDefaults.CenterControls(
            player = player,
            visible = visible,
            modifier = Modifier.fillMaxWidth(),
            backSecondary = {},
            forwardSecondary = {}
        )
    }
    ```
    Providing empty lambdas (`{}`) to `backSecondary` and `forwardSecondary` explicitly overrides the default behavior, hiding the skip controls entirely from the user interface. This directly explains why the controls are unusable.

## Root Cause
1. **Missing Extractor Flag**: Seeking within fMP4 containers lacks the required `FLAG_MERGE_FRAGMENTED_SIDX` flag in `DefaultExtractorsFactory`, preventing the player from building a valid seek map from the `mfra` box.
2. **Hidden UI Controls**: The `backSecondary` and `forwardSecondary` slots in `PlayerDefaults.CenterControls` were assigned empty lambdas, hiding the skip buttons from the UI.

## Proposed Fix
1. **Update Extractor Flags**: In both `VideoViewer` and `AudioViewer` in `SpecificViewers.kt`, append `androidx.media3.extractor.mp4.FragmentedMp4Extractor.FLAG_MERGE_FRAGMENTED_SIDX` to the flags passed to `setFragmentedMp4ExtractorFlags`.
2. **Restore UI Controls**: Remove the `backSecondary = {}` and `forwardSecondary = {}` arguments from the `PlayerDefaults.CenterControls` calls in both viewers, allowing the default skip buttons to render and function.

## Test Plan
1. **Build the Portal App**: Ensure the `:04_portal:app` module builds successfully.
2. **Run Tests**: Execute `./gradlew :04_portal:app:test` to verify no regressions.
3. **Manual/Visual Verification**: Confirm the skip controls are now visible and that the player can seek within an fMP4 file correctly.

## Test Results
- **Build the Portal App**: The `:04_portal:app` module compiled successfully.
- **Run Tests**: Executed `./gradlew :04_portal:app:test` which completed successfully with no failures.
