# Sakshi Vault (साक्षी वॉल्ट)

[![Release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Vault?include_prereleases)](https://github.com/RajnishKMehta/Sakshi-Vault/releases)
[![Build Status](https://github.com/RajnishKMehta/Sakshi-Vault/actions/workflows/checks.yml/badge.svg)](https://github.com/RajnishKMehta/Sakshi-Vault/actions/workflows/checks.yml)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android%20minSdk%2029%20compileSdk%2037-green.svg?logo=android)](https://developer.android.com)

**Sakshi Vault** ("Sakshi (साक्षी)" = "Witness" in Sanskrit) is the official secure storage application of the open-source Sakshi Android ecosystem. It is a highly specialized background service application designed to receive and protect important photos and videos captured by client applications.

---

## Why Sakshi Vault Exists

During protests, accidents, or incidents involving abuse of power, individuals capture critical evidence with normal Camera apps. However, storing the only copy of that recording locally inside the capturing app is a single point of failure. If an adversary forces the user to unlock their phone and delete the file, the evidence is lost forever.

**Sakshi splits responsibilities**:
- **Capture Apps** (Camera/Audio): capture media and display preview.
- **Sakshi Vault** (this app): runs as a separate background application. While the camera app is recording, it streams bytes to the Vault over AIDL IPC, which creates its own protected, private copy. If someone later deletes the recording from the capture app, the Vault copy remains secure.

---

## Features
- **Secure Background Sync:** Streams photos and videos securely in the background using Android's Binder IPC (AIDL).
- **Incremental Video Storage:** Captures ongoing video files safely.
- **Metadata Management:** Stores metadata using Room database securely on the device.
- **Independent Architecture:** Operates completely separate from the capture applications, making deletion difficult for bad actors.
- **Offline Capable:** Does not require internet connectivity to function and secure your files.

## Architecture

Sakshi ecosystem relies on three core components:
1. **Sakshi Vault** (This repo) - The storage engine.
2. **Sakshi SDK** - The AIDL/IPC middleware that handles communication.
3. **Client Apps** - E.g., Sakshi Camera, Audio Recorder.

Vault listens for intents via `rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE` and receives data chunks from client apps using standard Binder transactions.

## Integration

If you want to build a client app that sends data to Sakshi Vault, or if you want to understand how this Vault integrates with the overall ecosystem, please check out our integration guide:

👉 [Integration & Developer Guide](docs/INTEGRATION.md)

## Building & Testing

To build the project locally, standard Gradle commands apply:

```bash
# Debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run lint and code checks
./gradlew check
```

*Note on Debug Builds: GitHub Actions workflows dynamically set the `versionCode` for debug builds based on the action run number.*

## Contributing
We welcome contributions! Please review the [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for details on our code of conduct and the process for submitting Pull Requests.

## License
This project is licensed under the Apache 2.0 License - see the [LICENSE](LICENSE) file for details.
