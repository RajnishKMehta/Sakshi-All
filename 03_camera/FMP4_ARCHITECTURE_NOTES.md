# Fragmented MP4 (fMP4) Architecture Notes

This document provides details on specific architectural decisions and behaviors related to Fragmented MP4 (fMP4) video recording in the Sakshi Camera application.

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

## 2. fMP4 Upload/Compatibility Issues on Web Platforms

**Symptom:**
fMP4 videos record successfully and play perfectly within local video players. However, when attempting to upload the video to certain websites or web platforms, the file may show as `0 bytes`, or the platform rejects the file with a generic "Something went wrong" error.

**Reason:**
This occurs due to the structural differences between Standard MP4 and Fragmented MP4 containers:

1. **Missing Global MOOV Atom:** Standard MP4 files have a single, global `moov` (movie) atom at the beginning or end of the file containing the index and metadata for the entire video. Fragmented MP4s, however, lack this global index. Instead, they are broken into smaller segments, each with its own `moof` (movie fragment) and `mdat` (media data) atoms.
2. **Web Player/Platform Compatibility:** Many web uploaders, web players, and older parsers strictly look for the global `moov` atom to determine video duration, dimensions, and validity. When they encounter an fMP4 file missing this atom, they fail to parse the file structure, interpreting it as an empty (`0 bytes`) or corrupt file.
3. **MediaStore Caching (Less Common):** If the file is uploaded while it is still marked as `IS_PENDING` in the Android MediaStore, external applications might only see an empty placeholder. However, the primary reason for upload rejection on the web is the lack of a traditional `moov` atom.
