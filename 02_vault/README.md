<div id="top" align="center">

# Sākṣī Vault (साक्षी वॉल्ट)

[![Release](https://img.shields.io/github/v/release/RajnishKMehta/Sakshi-Vault?include_prereleases&logo=github)](https://github.com/RajnishKMehta/Sakshi-Vault/releases)
[![Build Status](https://github.com/RajnishKMehta/Sakshi-Vault/actions/workflows/checks.yml/badge.svg)](https://github.com/RajnishKMehta/Sakshi-Vault/actions/workflows/checks.yml)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?logo=apache)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Lang-kotlin-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android_10+-green.svg?logo=android)](https://developer.android.com)

</div>


**Sākṣī Vault** ("Sākṣī (साक्षी)" = "Witness" in Sanskrit) is the secure storage application of the open-source Sākṣī Android ecosystem.

It runs as a separate background application without a user interface, dedicated to receiving and protecting media from client applications such as Sākṣī Camera over local IPC.

---

## Why Sākṣī Vault Exists

Sākṣī Vault is designed to keep a separate, independent copy of important media captured by another app.

Instead of keeping the only copy inside the camera or recorder, a client app can send the captured media to Sākṣī Vault over Android IPC. This keeps the storage layer entirely separate from the capture app and viewing interfaces.

---

## Features

- **Background-Only Execution:** Operates entirely in the background without any UI to minimize the surface area for compromise.
- **Media Transfer:** Receives photos, videos, and audio from client applications over Android Binder IPC (AIDL).
- **Secure Local Storage:** Stores received media privately on the device in a separate app container.
- **Metadata Management:** Stores file metadata and synchronization state using Room 3.
- **Scheduled Deletion:** Handles delayed deletion. Deletion requests immediately hide the media but permanently remove the Vault copy only after a set delay (30 days) using WorkManager and monotonic clock validation, resisting simple time-tampering. The `DeletionEligibilityChecker` uses elapsed realtime only when boot metadata is valid and falls back to wall-clock time when metadata is missing or mismatched, or elapsed realtime moves backwards.
- **Independent Architecture:** Runs completely separately from the applications that capture or view the media.
- **Offline Capable:** Requires no internet connection, keeping data local.

---

## Ecosystem Architecture

The Sākṣī ecosystem connects components locally:

1. **[Sākṣī SDK](../../../../Sakshi-SDK)** — provides the AIDL/IPC interface.
2. **Sākṣī Vault** (this repo) — safely stores received media as an external service.
3. **Client Apps** — such as [Sākṣī Camera](../../../../Sakshi-Camera) and [Sākṣī Portal](../../../../Sakshi-Portal).

Vault exposes the `rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE` service to receive IPC calls securely.

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


## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
