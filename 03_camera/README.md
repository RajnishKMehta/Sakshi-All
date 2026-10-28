# Sākṣī Camera

Sākṣī Camera is the media capture application of the Sākṣī Android ecosystem.

It can be used as a standalone camera application or integrated with the Sākṣī ecosystem to securely transfer media to the independent Sākṣī Vault while recording.

## Features

- **Photo & Video Capture:** Records photos and videos using the device camera.
- **Fragmented MP4 (fMP4) Support:** Natively supports fMP4 for crash-safe, interruption-resilient recording.
- **Incremental Synchronization:** When recording fMP4, Sākṣī Camera synchronizes data incrementally to Sākṣī Vault while the recording is still in progress, helping to preserve data even if the process is unexpectedly interrupted.
- **Standard MP4 Recording:** Also supports standard MP4 recording. Unlike fMP4, standard MP4 is only transferred to the Vault after the entire recording finishes.
- **Local-Only Operation:** Requires no internet connection.
- **XML Architecture:** Built using traditional XML layouts and Material 3 Views.

## Recording and Synchronization

Sākṣī Camera uses modern Jetpack CameraX and AndroidX Media3 APIs.

The container format dictates synchronization behavior:

- **fMP4 (Default):** Writes data in fragments. Using the Sākṣī SDK, the camera incrementally synchronizes the video to Sākṣī Vault while recording is ongoing. This provides a better chance of recovering evidence if the camera is forcibly shut down before stopping the recording normally.
- **Standard MP4:** Not crash-safe and cannot be synchronized incrementally. Standard MP4 recordings are only copied to the Vault after the recording has completely finished.

By separating capture from storage, deleting a completed recording in the Camera app does not itself delete the preserved copy in the Vault.

## Building from Source

To build Sākṣī Camera from source, use standard Gradle commands:

```bash
# Build Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run lint and code checks
./gradlew check
```

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
