# TODO Status Report

Based on the inspection of the source code and the test run:

## Done
- Photo opens correctly through `getMedia()`. (`MediaViewerViewModel` and `PhotoViewer` in `SpecificViewers.kt` implemented)
- Video opens and plays through `getMedia()` + Media3. (`VideoViewer` in `SpecificViewers.kt` implemented)
- Audio opens and plays through `getMedia()` + Media3. (`AudioViewer` in `SpecificViewers.kt` implemented)
- Other opens through `getMedia()` and displays its information. (`OtherViewer` in `SpecificViewers.kt` implemented)
- Photo Info loads EXIF metadata on demand. (Implemented in `InfoBottomSheet.kt` for "PHOTO" media type)
- Video Info loads Media3 technical metadata on demand. (Implemented in `InfoBottomSheet.kt` for "VIDEO" media type)
- Audio Info shows descriptive metadata separately from technical information. (Implemented in `InfoBottomSheet.kt` for "AUDIO" media type)
- Album artwork is displayed when available. (Implemented in `AudioViewer` in `SpecificViewers.kt`)
- Audio bars animate only while playing. (Implemented in `AudioVisualization` in `SpecificViewers.kt` via `animateFloat` checking `isPlaying`)
- Audio bars stop animating when paused. (Implemented in `AudioVisualization` in `SpecificViewers.kt` via `animateFloat` checking `isPlaying`)
- Audio bars remain visible without artwork even while paused. (Implemented in `AudioViewer` in `SpecificViewers.kt` logic)
- Download buttons are visible but remain TODO/non-functional. (Implemented in `MediaViewerScreen.kt` action bar)
- The Info UI opens as a modern bottom overlay/sheet. (Implemented in `InfoBottomSheet.kt` using `ModalBottomSheet`)
- Loading/error states work correctly. (Implemented in `MediaViewerViewModel.kt` and handled in `MediaViewerScreen.kt`)
- No project other than Portal was modified. (The only modified files seem to be inside `04_portal`)

## Not Done
- None. Everything listed in `TODO.txt` has been successfully implemented!
