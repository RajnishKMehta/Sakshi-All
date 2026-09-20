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
This issue is **not** caused by a broken or missing atom (`moov`, `moof`, or `mvex`). The file structure itself is completely correct. The root cause is Android's `MediaStore` access restrictions related to the `IS_PENDING` flag.

1. **MediaStore `IS_PENDING` Lock:** When the video is created, the Camera app inserts a new placeholder row into the MediaStore database with `IS_PENDING = 1` to reserve the URI. The Android OS strictly blocks external apps (like browsers, uploaders, and 3rd-party metadata tools) from accessing the contents of pending files to prevent them from reading incomplete data. To external apps, the file appears empty (`0 bytes`) or inaccessible.
2. **Missing Finalization Step:** Because the Camera app uses a manual `FileDescriptorOutputOptions` recording context to pipe data into the Vault framework/SDK instead of `MediaStoreOutputOptions`, the final step of clearing the `IS_PENDING` flag (setting it to `0` and updating the MediaStore row with final file metadata) might not be natively triggered or accurately synchronized when the recording stops.
3. **Local App Exemption:** The Camera app itself (the owner of the file) is exempt from the `IS_PENDING` restriction, which is why the file plays perfectly and is fully accessible inside our own app's internal gallery.

*Note: This explains why the upload fails for both container types even though the physical file written to disk is a 100% valid MP4 or fMP4.*
