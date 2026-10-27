<div id="top" align="center">

# Sākṣī Camera (साक्षी कैमरा)

[![Release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Camera?include_prereleases&logo=github)](https://github.com/RajnishKMehta/Sakshi-Camera/releases)
[![Build Status](https://github.com/RajnishKMehta/Sakshi-Camera/actions/workflows/checks.yml/badge.svg)](https://github.com/RajnishKMehta/Sakshi-Camera/actions/workflows/checks.yml)

[![License](https://img.shields.io/github/license/RajnishKMehta/Sakshi-Camera?logo=apache&logoColor=DDC06F)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Lang-kotlin-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android_10+-green.svg?logo=android)](https://developer.android.com)

</div>


**Sākṣī Camera** ("Sākṣī (साक्षी)" = "Witness" in Sanskrit) is the camera application of the open-source Sākṣī Android ecosystem.

It is designed to capture media and work with **Sākṣī Vault** to preserve an independent local copy while recording.

---

## Why Sākṣī Camera Exists

A camera app is the starting point of the Sākṣī ecosystem. Sākṣī Camera strictly handles capture, utilizing the Sākṣī SDK to securely transfer media over local IPC to the independent Sākṣī Vault.

By separating capture from storage, the ecosystem ensures that if the Camera app is compromised or a user is forced to delete their recording in the camera interface, the Vault independently preserves the evidence.

---

## Features

- **Photo & Video Capture:** Capture photos and videos using the device camera.
- **Crash-Safe fMP4 Support:** Supports Fragmented MP4 (fMP4) natively for interruption-resilient recording.
- **Incremental Transfer:** Sends captured photos and incrementally synchronizes fMP4 videos to Sākṣī Vault through the Sākṣī SDK *while the recording is happening*.
- **Local-only:** No internet connection needed, prioritizing user privacy and local security.
- **XML Architecture:** Built with reliable XML layouts and Material 3 Views.

---

## Recording Architecture

Sākṣī Camera uses modern Jetpack CameraX and AndroidX Media3 APIs for video recording:

1. **Fragmented MP4 (fMP4) Recording (Default)**: Implements a custom, crash-safe recording pipeline using Media3's `FragmentedMp4Muxer` injected via CameraX's `MuxerFactory` interface. Because fMP4 writes data in fragments, this preserves data integrity upon unexpected terminations or interruptions.
2. **Standard MP4 Recording**: Utilizes the default official CameraX `Recorder` implementation via the platform muxer.

The chosen container format dictates the Vault synchronization behavior:

- **fMP4**: Automatically streams to Vault while recording using the SDK's incremental audio/video synchronization API.
- **Normal MP4**: Not crash-safe, so it cannot be synced incrementally. It is only transferred to Vault after the entire recording finishes, using the SDK's standard file copy API. Switching to normal MP4 requires user confirmation of this security trade-off.

---

## Ecosystem Architecture

Sākṣī Camera is one integrated part of the ecosystem:

1. **Sākṣī Camera** (this repo) — captures media.
2. **[Sākṣī SDK](../../../../Sakshi-SDK)** — provides the client-side API and IPC layer.
3. **[Sākṣī Vault](../../../../Sakshi-Vault)** — safely stores received media independently.

```text
Sākṣī Camera (Capture)
      │
      │ Sākṣī SDK (Local IPC)
      │
      ▼
Sākṣī Vault (Secure Independent Storage)
```

---

## Building & Testing

Standard Gradle commands can be used to build and test the project:

```bash
# Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run lint and code checks
./gradlew check
```


---

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
