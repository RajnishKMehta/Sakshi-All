# Why Fragmented MP4 is Broken

## The Issue
Fragmented MP4 (FMP4) files captured by the Camera app fail to play correctly in most standard Android media players and the Portal app. They open, but do not progress, show incomplete duration, and have no sound. They only work in VLC, which uses a more robust custom demuxer capable of piecing together malformed media files.

## The Reason
The root cause lies in how `FragmentedMedia3Muxer` processes Android `MediaCodec` buffer flags.

When writing sample data, the native Android `MediaCodec.BufferInfo` includes flags such as `BUFFER_FLAG_CODEC_CONFIG` (value 2), which indicates the buffer contains codec initialization data (like SPS/PPS for H.264), not actual media samples.

However, `FragmentedMedia3Muxer` was blindly passing these `MediaCodec` flags directly into `Media3BufferInfo`. Media3's `FragmentedMp4Muxer` treats these `CODEC_CONFIG` buffers as normal video/audio samples because it expects its own `androidx.media3.common.C.BUFFER_FLAG_*` constants.

Since `CODEC_CONFIG` is written as a sample, the internal timeline and byte offsets in the FMP4 `moof`/`mdat` fragments become corrupted. Standard Android players (like ExoPlayer/Media3) read these corrupted fragments, fail to decode the "config" sample, and freeze playback because the timestamps and sample sizes are misaligned. VLC survives this by aggressively scanning the stream and dropping invalid samples.

## The Fix
1. **Filter Codec Config:** In `FragmentedMedia3Muxer.kt`, we must explicitly drop any sample where `(bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0`. The initialization data is already handled in `addTrack()`.
2. **Map Flags Correctly:** We must map the Android `MediaCodec` flags (`BUFFER_FLAG_KEY_FRAME` and `BUFFER_FLAG_END_OF_STREAM`) strictly to their corresponding `androidx.media3.common.C.BUFFER_FLAG_*` equivalents when constructing the `Media3BufferInfo`.
