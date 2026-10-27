# Sākṣī Camera

Sākṣī Camera is the specialized capture application of the open-source Sākṣī Android ecosystem.

It is designed to capture media and work transparently with Sākṣī Vault to preserve an independent local copy while recording.

## About

Sākṣī Camera serves as the starting point of the ecosystem. It strictly handles media capture, utilizing the Sākṣī SDK to securely transfer media over local IPC to the independent Sākṣī Vault. By separating capture from storage, deleting a recording in the Camera app does not inherently delete the completed Vault copy, providing a safety net when a user is forced to delete evidence.

## Features

- **Photo & Video Capture**: Capture photos and videos using the device camera.
- **Crash-Safe fMP4 Support**: Supports Fragmented MP4 (fMP4) natively for interruption-resilient recording.
- **Incremental Transfer**: Sends captured photos and incrementally synchronizes fMP4 videos to Sākṣī Vault through the SDK *while the recording is happening*.
- **Local-Only**: Operates completely offline, prioritizing user privacy and local security.
- **XML Architecture**: Built with reliable XML layouts and Material 3 Views.

## Architecture / How It Works

Sākṣī Camera integrates modern Jetpack CameraX and AndroidX Media3 APIs to enable secure recording and synchronization:

- **Fragmented MP4 (fMP4)**: By default, the camera uses a custom, crash-safe pipeline using Media3's `FragmentedMp4Muxer`. Because fMP4 writes data in fragments, this preserves data integrity even during unexpected interruptions. fMP4 streams to the Vault automatically while recording via the SDK's incremental sync API.
- **Standard MP4**: Standard MP4 is supported but is not crash-safe. It is only transferred to Vault after the entire recording finishes, using the SDK's standard file copy API.
- **Ecosystem Flow**: Sākṣī Camera records media -> Sākṣī SDK handles local IPC -> Sākṣī Vault provides secure independent storage.

## Requirements / Compatibility

- Minimum Android Version: Android 10 (API Level 29)
- Language: Kotlin

## Security and Privacy

While Sākṣī Camera automatically mirrors recordings to the Vault, it is important to note that the Vault's API allows connected apps to schedule deletions. Sākṣī does not guarantee absolute evidence preservation if the device is compromised or physically destroyed.

## License

This project is licensed under the Apache License 2.0. See the LICENSE file for details.
