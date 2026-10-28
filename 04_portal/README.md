# Sākṣī Portal

Sākṣī Portal is the user-facing interface for the Sākṣī ecosystem. It allows users to safely browse, view, and manage media stored independently in the Sākṣī Vault.

## About

Sākṣī helps people retain important media and evidence by keeping a separate copy beyond the ordinary gallery or file manager. While the Vault acts as a background-only storage layer, Sākṣī Portal acts as the secure gateway to access those files.

By separating the viewing interface from the storage engine, the ecosystem maintains strict boundaries, ensuring the Vault remains entirely hidden from normal view. Sākṣī Portal can be installed, used to manage files, and then uninstalled to reduce casual visibility, while leaving the Vault's data intact.

## Features

- **Media Browsing:** Securely browse photos and videos preserved in the Sākṣī Vault.
- **Media Playback:** Built-in viewer for photos and seamless playback of standard MP4 and interruption-resilient Fragmented MP4 (fMP4) videos using AndroidX Media3.
- **Export and Manage:** Export preserved media out of the Vault to the device's public storage using modern `MediaStore` APIs.
- **App Lock:** Secure the application using built-in Android biometric authentication (`androidx.biometric`).
- **Modern UI:** Built entirely with Jetpack Compose and Navigation 3 for a responsive experience.
- **Local-Only:** Requires no internet connection, keeping your data local and secure.

## Architecture

Sākṣī Portal connects to Sākṣī Vault to fetch and display the user's stored media:

1. **Sākṣī Camera:** Captures the media.
2. **Sākṣī SDK:** Handles the API and IPC layer between apps.
3. **Sākṣī Vault:** Safely stores media independently in the background.
4. **Sākṣī Portal (This Project):** Connects to the Vault via the SDK to browse, view, and export stored media.

If you are a developer looking to integrate or extend functionality, refer to the documentation in the Sākṣī SDK repository.

## Build From Source

To build Sākṣī Portal locally:

```bash
./gradlew assembleDebug
```

## Security and Privacy

Sākṣī Portal provides an interface to access media stored in the Vault. It is not designed to provide absolute device-level security. Uninstalling Sākṣī Portal makes accessing the Vault harder for casual observers, but it does not make the Vault or its data undetectable to forensic tools, MDM profiles, or users with root access.

## License

This project is licensed under the Apache License 2.0.
