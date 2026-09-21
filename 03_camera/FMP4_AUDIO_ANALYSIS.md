# Analysis: Missing Audio in FMP4 Recordings

## 1. Problem
When recording in fragmented MP4 (FMP4) format, the output file contains video but no audio, whereas standard MP4 correctly includes both.

## 2. MP4 vs FMP4 Audio Flow
- **Standard MP4 Flow:** Uses CameraX's native `Recorder` and native muxer implementation. `VideoCapturer` calls `pendingRecording.withAudioEnabled()` if audio is enabled. The native muxer receives both video and audio tracks directly from the CameraX recording pipeline.
- **FMP4 Flow:** We configure CameraX to use a custom muxer factory (`FragmentedMedia3MuxerFactory`). CameraX's `Recorder` still processes the audio (via `withAudioEnabled()`), but it routes the output format definitions and encoded sample buffers through our custom `FragmentedMedia3Muxer` using `addTrack()` and `writeSampleData()`.

## 3. Root Cause / Exact Point Where Audio is Missing
The audio appears to be lost or rendered unplayable during or after `FragmentedMedia3Muxer.addTrack()`.

When translating Android's `MediaFormat` into Media3's `Format` object, we map `KEY_MIME`, `KEY_CHANNEL_COUNT`, `KEY_SAMPLE_RATE`, and `KEY_BIT_RATE`. It is hypothesized that missing audio-specific properties during this translation might result in an invalid or unrecognized audio track in the final FMP4 file. For example, Android's `MediaFormat` might contain fields that could map to Media3 `Format.Builder` fields, such as:
- `KEY_LANGUAGE` mapping to `Format.Builder.setLanguage()`
- `KEY_MAX_INPUT_SIZE` mapping to `Format.Builder.setMaxInputSize()`
- `KEY_PROFILE` or `KEY_AAC_PROFILE` mapping to an RFC 6381 codec string for `Format.Builder.setCodecs()`

It is possible that the omission of these fields in the `Format` provided to Media3 results in an FMP4 audio track that players cannot properly decode.

Furthermore, we are writing MP4 metadata unconditionally when the *first* track is added:
```kotlin
        if (!metadataAdded) {
            // adds orientation, location, fps metadata
            metadataAdded = true
        }
```
If the audio track happens to be added first, applying video-specific metadata (`Mp4OrientationData`, `captureFps`) to the global muxer *before* or *during* an audio track initialization might be incorrect, though this is also a hypothesis to be verified.

## 4. Evidence from Current Code
In `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/capturer/FragmentedMedia3Muxer.kt`:
```kotlin
    override fun addTrack(format: android.media.MediaFormat): Int {
        val mimeType = format.getString(android.media.MediaFormat.KEY_MIME)
        val formatBuilder = Format.Builder().setSampleMimeType(mimeType)
        // ... extracts width, height, channel count, sample rate, bit rate ...
```
We do not check for or map audio-specific configuration keys like `KEY_LANGUAGE`, `KEY_MAX_INPUT_SIZE`, or `KEY_PROFILE`.

Additionally, `FragmentedMedia3Muxer`'s `metadataAdded` flag:
```kotlin
        if (!metadataAdded) {
            orientationDegrees?.let { degrees ->
                currentMuxer.addMetadataEntry(Mp4OrientationData(degrees))
            }
            // ...
            metadataAdded = true
        }
```
This is executed indiscriminately regardless of whether the incoming format is audio or video (`mimeType.startsWith("video/")`).

## 5. What Would Need to Be Changed Later
- Differentiate between audio and video tracks inside `addTrack()`.
- Investigate and map required audio keys (e.g., `KEY_LANGUAGE` to `setLanguage()`, `KEY_MAX_INPUT_SIZE` to `setMaxInputSize()`, and `KEY_PROFILE` to `setCodecs()`) if they are necessary for successful audio playback.
- Ensure video metadata (`Mp4OrientationData`, `KEY_ANDROID_CAPTURE_FPS`) is only added when processing the video track (or added globally in a safe manner).

## 6. Relevant Media3 Limitations
Media3's `FragmentedMp4Muxer` builds `moof`/`traf` boxes based strictly on the provided `Format`. If the format object lacks essential audio profile details that Android's native `MediaMuxer` would otherwise infer directly from the `MediaFormat`, the resulting FMP4 chunks might have incomplete audio descriptions, potentially leading players to ignore the audio track entirely.


## 7. Resolution
The issue has been resolved by:
1. Updating `FragmentedMedia3Muxer.kt` to extract `KEY_LANGUAGE` and `KEY_MAX_INPUT_SIZE` from the Android `MediaFormat`.
2. Ensuring video-specific metadata (`Mp4OrientationData`, `KEY_ANDROID_CAPTURE_FPS`, `Mp4LocationData`) is added *only* when the video track is initialized.
3. Constructing an `AudioSpecificConfig` based on `KEY_PROFILE` (or `KEY_AAC_PROFILE`), sample rate, and channel count, and providing it via `Format.initializationData`. This allows Media3's `esdsBox` builder to correctly populate the codec profile without relying on `setCodecs`.

With these changes, the custom muxer accurately constructs `moof`/`traf` and `esds` boxes for FMP4 files, making the audio track playable.
