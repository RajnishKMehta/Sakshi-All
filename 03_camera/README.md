<div id="top" align="center">

# Sakshi Camera (साक्षी कैमरा)

[![Release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Camera?include_prereleases&logo=github)](https://github.com/RajnishKMehta/Sakshi-Camera/releases)
[![Build Status](https://github.com/RajnishKMehta/Sakshi-Camera/actions/workflows/checks.yml/badge.svg)](https://github.com/RajnishKMehta/Sakshi-Camera/actions/workflows/checks.yml)

[![License](https://img.shields.io/badge/License-MIT-blue.svg?logo=mit)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Lang-kotlin-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android_10+-green.svg?logo=android)](https://developer.android.com)

</div>

> [!WARNING]
> **Early Beta:** Sakshi Camera currently supports **photo and video capture and transfer**. Audio functionality is not implemented yet. APIs and behavior may change in future releases.

**Sakshi Camera** ("Sakshi (साक्षी)" = "Witness" in Sanskrit) is the camera application of the open-source Sakshi Android ecosystem.

It is designed to capture media and work with Sakshi Vault when an independent local copy is needed.

---

## Why Sakshi Camera Exists

A camera app is the starting point of the Sakshi ecosystem. Sakshi Camera handles capture, while the Sakshi SDK provides communication with Sakshi Vault.

Keeping these responsibilities separate lets the capture app focus on the camera while Vault handles independent storage.

---

## Features

- **Photo & Video Capture:** Capture photos and videos using the device camera.
- **Photo & Video Transfer:** Send captured photos and videos to Sakshi Vault through the Sakshi SDK.
- **Local-First:** The capture and transfer flow does not depend on an internet connection.
- **Sakshi Integration:** Works with the Sakshi SDK and Sakshi Vault as part of the Sakshi ecosystem.

> **Currently unavailable:** Audio capture/transfer is not implemented yet.

---

## Architecture

Sakshi Camera is one part of the Sakshi ecosystem:

1. **Sakshi Camera** (this repo) — captures media.
2. **[Sakshi SDK](../../../../Sakshi-SDK)** — provides the client-side API and IPC layer.
3. **[Sakshi Vault](../../../../Sakshi-Vault)** — receives and stores media independently.

The basic flow is:

```text
Sakshi Camera
      │
      │ Sakshi SDK
      │
      ▼
Sakshi Vault
      │
      ▼
Local storage
```

For the current beta, this flow supports photos and videos.

---

## Integration

Sakshi Camera uses the [Sakshi SDK](https://github.com/RajnishKMehta/Sakshi-SDK) to communicate with [Sakshi Vault](https://github.com/RajnishKMehta/Sakshi-Vault).

If you are building another client application for the Sakshi ecosystem, see the SDK documentation for integration details.

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

*Note on Debug Builds: GitHub Actions workflows dynamically set the `versionCode` for debug builds based on the action run number.*

---

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
