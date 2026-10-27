<div id="top" align="center">

# Sākṣī Portal (साक्षी पोर्टल)

[![Release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Portal?include_prereleases&logo=github)](https://github.com/RajnishKMehta/Sakshi-Portal/releases)
[![Build Status](https://github.com/RajnishKMehta/Sakshi-Portal/actions/workflows/checks.yml/badge.svg)](https://github.com/RajnishKMehta/Sakshi-Portal/actions/workflows/checks.yml)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?logo=apache)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Lang-kotlin-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android_10+-green.svg?logo=android)](https://developer.android.com)

</div>

**Sākṣī Portal** ("Sākṣī (साक्षी)" = "Witness" in Sanskrit) is the user-facing application for the open-source Sākṣī Android ecosystem. It provides the interface for browsing, viewing, and managing media safely stored in the independent **Sākṣī Vault**.

---

## Why Sākṣī Portal Exists

While **Sākṣī Camera** captures media and **Sākṣī Vault** safely stores it, an interface is required for users to interact with this stored media. **Sākṣī Portal** acts as this secure gateway.

By keeping the viewing and management UI separate from the Vault and Camera, the ecosystem maintains a strict separation of concerns, ensuring that the Vault remains a background-only service dedicated solely to secure storage.

---

## Features

- **Media Browsing:** Browse photos and videos securely stored within Sākṣī Vault.
- **Media Playback & Viewing:** Built-in viewer for photos and smooth playback of normal MP4 and crash-safe fMP4 video containers using AndroidX Media3.
- **Export & Management:** Securely export or manage your files. Relies on modern `MediaStore` APIs.
- **Modern UI:** Built fully with Jetpack Compose and Navigation 3 for a responsive and intuitive user experience.
- **Offline Capable:** Does not require an internet connection, keeping your media completely local and secure.

---

## Ecosystem Architecture

Sākṣī Portal connects with Sākṣī Vault to fetch and display the user's stored media.

1. **[Sākṣī Camera](../../../../Sakshi-Camera)** — captures media.
2. **[Sākṣī SDK](../../../../Sakshi-SDK)** — provides the API and IPC layer.
3. **[Sākṣī Vault](../../../../Sakshi-Vault)** — safely stores media independently.
4. **Sākṣī Portal** (this repo) — browses and manages the stored media.

---

## Integration

Sākṣī Portal utilizes the [Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK) for inter-process communication with [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault).

If you are a developer looking to integrate or extend functionality, refer to the SDK documentation.

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
