# Sākṣī Portal

Sākṣī Portal is the user-facing interface for the Sākṣī Android ecosystem.

It provides a secure, modern interface for users to browse, view, and manage media that is independently stored within Sākṣī Vault.

## Features

- **Media Browsing:** Securely browse photos and videos stored in the Vault.
- **Media Playback:** Built-in viewer for photos and smooth video playback (including crash-safe fMP4 and standard MP4 containers) using AndroidX Media3.
- **Media Management:** View, export, and manage files. Uses modern `MediaStore` APIs.
- **Modern UI:** Built entirely with Jetpack Compose and Navigation 3.
- **Offline Capable:** Operates entirely locally without requiring an internet connection.

## Role in the Ecosystem

Sākṣī Portal connects to Sākṣī Vault via the Sākṣī SDK to fetch and display the user's stored media.

By keeping the viewing and management UI separate from the Vault, the ecosystem ensures that the Vault remains a background-only service dedicated strictly to storage. If desired, a user can uninstall the Portal to remove the viewing interface, while the Vault safely retains the preserved media in the background.

## Building from Source

To build Sākṣī Portal from source, use standard Gradle commands:

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
