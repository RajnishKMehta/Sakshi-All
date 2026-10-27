# Sākṣī Portal

Sākṣī Portal is the user-facing application for the open-source Sākṣī Android ecosystem.

It acts as the secure gateway, providing a comprehensive interface for users to browse, view, and manage media safely isolated in the independent Sākṣī Vault.

## About

While Sākṣī Camera captures media and Sākṣī Vault safely stores it in the background, users need a way to access those preserved files when they are safe to do so. Sākṣī Portal fulfills this need. By keeping the viewing and management UI separate from the Vault itself, the ecosystem maintains a strict separation of concerns, ensuring the Vault remains a dedicated, headless storage service.

## Features

- **Media Browsing**: Browse photos and videos securely stored within Sākṣī Vault.
- **Media Playback & Viewing**: Built-in viewer for photos and smooth playback of normal MP4 and crash-safe fMP4 video containers using AndroidX Media3.
- **Export & Management**: Securely export or recover your files using modern `MediaStore` APIs.
- **Modern UI**: Built fully with Jetpack Compose and Navigation 3 for a responsive and intuitive user experience.
- **Offline Capable**: Requires no internet connection, keeping your media completely local and secure.

## Architecture / How It Works

Sākṣī Portal connects locally to Sākṣī Vault to fetch and display the user's stored media:

- **Ecosystem Flow**: Sākṣī Portal securely binds to the Sākṣī Vault service via the Sākṣī SDK. The Portal queries the Vault for available media, retrieves the necessary content URIs, and displays the media to the user without directly accessing the Vault's internal databases.

## Requirements / Compatibility

- Minimum Android Version: Android 10 (API Level 29)
- Language: Kotlin

## Security and Privacy

Sākṣī Portal manages locally stored data completely offline. While it provides tools to access and recover preserved media, the Sākṣī ecosystem does not promise absolute immunity against advanced targeted attacks, complete device destruction, or unauthorized physical access if the device lock is compromised.

## License

This project is licensed under the Apache License 2.0. See the LICENSE file for details.
