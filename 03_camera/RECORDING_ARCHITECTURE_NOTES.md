# Recording Architecture Notes

This document provides details on specific architectural decisions and behaviors related to video recording (both Standard MP4 and Fragmented MP4) in the Sakshi Camera application.

## 1. The `storedPfd` Garbage Collection Fix

During fMP4 video recording using the custom `FragmentedMedia3Muxer`, the app previously crashed around 3-5 seconds into the recording with a `java.io.IOException: Bad file descriptor`.

**Why did this happen?**
When CameraX initializes a custom `Muxer` using a `MuxerFactory`, it passes a duplicated `ParcelFileDescriptor` to the `setOutput()` method. Internally, Android's `ParcelFileDescriptor` uses a `CloseGuard` that automatically closes the underlying file descriptor during Garbage Collection (GC) if the object is no longer strongly referenced.
Since our custom muxer created a `FileOutputStream` directly from the inner `fileDescriptor` and allowed the `ParcelFileDescriptor` object itself to go out of scope, the JVM's Garbage Collector would run a few seconds later, finalizing the object and forcibly closing the file descriptor beneath us. The Media3 `FragmentedMp4Muxer` would then attempt to write to a closed stream, resulting in the crash.

**The Solution:**
```kotlin
storedPfd = parcelFileDescriptor
```
By assigning the `ParcelFileDescriptor` to a class-level variable (`storedPfd`), we maintain a strong reference to it for the entire duration of the recording. This prevents the Garbage Collector from finalizing the object, keeping the file descriptor open and valid until we explicitly release the muxer.

## 2. The "0 Bytes" Web Upload & Processing Issue

**Symptom:**
Both MP4 and fMP4 videos record successfully, play perfectly within the local video player, and have completely valid and intact file structures. However, when attempting to upload these videos to websites, or select them from external file pickers/browsers, the platform shows the file as `0 bytes` or throws a "Something went wrong" / "File format not supported" error. Additionally, metadata viewer apps might fail to extract basic container information (like `moov` or `moof` atoms) despite the video playing fine.

**Reason:**
This issue is **not** caused by a broken or missing atom (`moov`, `moof`, or `mvex`). The file structure itself is completely correct. The root cause is Android's `MediaStore` access restrictions related to the `IS_PENDING` flag, which was left active.

1. **MediaStore `IS_PENDING` Lock:** When the video is created, the Camera app inserts a new placeholder row into the MediaStore database with `IS_PENDING = 1` to reserve the URI. The Android OS strictly blocks external apps (like browsers, uploaders, and 3rd-party metadata tools) from accessing the contents of pending files to prevent them from reading incomplete data. To external apps, the file appears empty (`0 bytes`) or inaccessible.
2. **Missing Finalization Step:** Previously, the `IS_PENDING` flag was not cleared when recording finished using `FileDescriptorOutputOptions`. Because of this, the `MediaStore` index was never updated with the final file size, and external apps remained locked out indefinitely.
3. **The Fix:** The implementation has been updated so that when `VideoRecordEvent.Finalize` is received from the CameraX recording session, the application explicitly triggers `removePendingFlagFromUri` to set `IS_PENDING = 0`. This signals to the Android OS that the file is complete, automatically updating its size metadata and unlocking full read access for all external applications.
4. **Local App Exemption:** (Note: The Camera app itself was exempt from the `IS_PENDING` restriction as the file's owner, which is why the file always played perfectly inside the internal gallery even while external apps saw 0 bytes).
