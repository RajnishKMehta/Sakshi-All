# Recording and Synchronization

Sākṣī Camera captures media and securely synchronizes a separate copy to the Sākṣī Vault. The behavior depends on the video format you choose in the camera settings.

## Fragmented MP4 (fMP4) - Default & Recommended

By default, Sākṣī Camera records video in the [Fragmented MP4](https://liveapi.com/blog/fragmented-mp4/) (fMP4) format. This format is crash-safe and designed for high-risk situations.

**How it works:**
* As you record, the video is split into tiny fragments.
* Sākṣī Camera synchronizes these fragments to the Vault *while the recording is still in progress*.
* If your phone suddenly turns off, crashes, or the app is force-closed, the fragments that have already been sent to the Vault are preserved.

## Normal MP4

You can switch the video format to Normal MP4 in the settings. This format is widely compatible but **lacks crash safety**.

**How it works:**
* Normal MP4 videos are **not** copied to the Vault while recording is in progress.
* The video is only copied to the Vault *after* you have finished recording and pressed the stop button just like photo.
* If the app closes or the phone turns off while recording, the video may be completely lost.
