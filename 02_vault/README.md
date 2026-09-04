<div id="top" align="center">

# Sakshi Vault (साक्षी वॉल्ट)

[![Release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Vault?include_prereleases&logo=github)](https://github.com/RajnishKMehta/Sakshi-Vault/releases)
[![Build Status](https://github.com/RajnishKMehta/Sakshi-Vault/actions/workflows/checks.yml/badge.svg)](https://github.com/RajnishKMehta/Sakshi-Vault/actions/workflows/checks.yml)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?logo=apache)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Lang-kotlin-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android_10+-green.svg?logo=android)](https://developer.android.com)

</div>

> [!WARNING]
> **Early Beta:** Sakshi Vault currently supports **photo, video, and audio transfer**. APIs and behavior may change in future releases.

**Sakshi Vault** ("Sakshi (साक्षी)" = "Witness" in Sanskrit) is the secure storage application of the open-source Sakshi Android ecosystem.

It runs as a separate background application and receives media from client applications such as Sakshi Camera.

---

## Why Sakshi Vault Exists

Sakshi Vault is designed to keep a separate copy of important media captured by another app.

Instead of keeping the only copy inside the camera or recorder, a client app can send the captured media to Sakshi Vault over Android IPC. This keeps the storage layer separate from the capture app.

---

## Features

- **Media Transfer:** Receives photos, videos, and audio from client applications over Android Binder IPC (AIDL).
- **Secure Local Storage:** Stores received media privately on the device.
- **Metadata Management:** Stores file metadata using Room.
- **Independent Architecture:** Runs separately from the applications that capture the media.
- **Offline Capable:** Does not require an internet connection.

---

## Architecture

The Sakshi ecosystem consists of three main components:

1. **Sakshi Vault** (this repo) — stores received media.
2. **[Sakshi SDK](https://github.com/RajnishKMehta/Sakshi-SDK)** — provides the AIDL/IPC interface used by client apps.
3. **Client Apps** — such as Sakshi Camera or future audio/video applications.

Vault exposes the `rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE` service and receives data from client applications through Android Binder IPC.

---

## Integration

If you want to build a client app that sends media to Sakshi Vault, see the integration guide:

👉 [Integration & Developer Guide](docs/INTEGRATION.md)

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

***Note on Debug Builds**: GitHub Actions workflows dynamically set the versionCode for debug builds based on the action run number.*

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.
