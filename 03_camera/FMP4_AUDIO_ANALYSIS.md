# Analysis: Missing Audio in FMP4 Recordings

## 1. Problem
When recording in fragmented MP4 (FMP4) format, the output file contains video but no audio, whereas standard MP4 correctly includes both.

## 2. MP4 vs FMP4 Audio Flow
- **Standard MP4 Flow:** Uses CameraX's native `Recorder` and native muxer implementation. `VideoCapturer` calls `pendingRecording.withAudioEnabled()` if audio is enabled. The native muxer receives both video and audio tracks directly from the CameraX recording pipeline.
- **FMP4 Flow:** We configure CameraX to use a custom muxer factory (`FragmentedMedia3MuxerFactory`). CameraX's `Recorder` still processes the audio (via `withAudioEnabled()`), but it routes the output format definitions and encoded sample buffers through our custom `FragmentedMedia3Muxer` using `addTrack()` and `writeSampleData()`.

## 3. Root Cause / Exact Point Where Audio is Missing
The audio is lost inside `FragmentedMedia3Muxer.addTrack()`.

When translating Android's `MediaFormat` into Media3's `Format` object, we are missing a critical property for audio. While we translate `KEY_MIME`, `KEY_CHANNEL_COUNT`, `KEY_SAMPLE_RATE`, and `KEY_BIT_RATE`, we **fail to translate the audio profile or encode the audio peak bitrates/max input size**, which might cause Media3 to reject or improperly mux the audio track.

But more importantly, Media3's `Mp4Muxer`/`FragmentedMp4Muxer` documentation for adding tracks states that for audio formats (like AAC), **the `Language` or `Profile` (such as AAC profile)** might be required for the track to be successfully parsed by players.

Furthermore, we are writing MP4 metadata unconditionally when the *first* track is added:
```kotlin
        if (!metadataAdded) {
            // adds orientation, location, fps metadata
            metadataAdded = true
        }
```
If the audio track happens to be added first, applying video-specific metadata (`Mp4OrientationData`, `captureFps`) to the global muxer *before* or *during* an audio track initialization could potentially corrupt the headers or cause player incompatibility.

## 4. Evidence from Current Code
In `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/capturer/FragmentedMedia3Muxer.kt`:
```kotlin
    override fun addTrack(format: android.media.MediaFormat): Int {
        val mimeType = format.getString(android.media.MediaFormat.KEY_MIME)
        val formatBuilder = Format.Builder().setSampleMimeType(mimeType)
        // ... extracts width, height, channel count, sample rate, bit rate ...
```
We do not check for or map audio-specific configuration keys like `KEY_AAC_PROFILE` or `KEY_PROFILE`.

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
- For audio tracks, extract and apply `KEY_PROFILE` or `KEY_AAC_PROFILE` if present.
- Ensure video metadata (`Mp4OrientationData`, `KEY_ANDROID_CAPTURE_FPS`) is only added when processing the video track (or added globally in a safe manner that doesn't conflict with audio track addition).
- Ensure that the audio `MediaFormat`'s `Language` tag (if any) is correctly passed to the Media3 `Format.Builder()`.

## 6. Relevant Media3 Limitations
Media3's `FragmentedMp4Muxer` builds `moof`/`traf` boxes based strictly on the provided `Format`. If the format object lacks essential audio profile details that Android's native `MediaMuxer` would otherwise infer directly from the `MediaFormat`, the resulting FMP4 chunks will have malformed audio descriptions, leading players to ignore the audio track entirely.
