# Sākṣī Vault

Sākṣī Vault is the secure, independent storage application of the open-source Sākṣī Android ecosystem.

It runs entirely as a background service without a user interface, dedicated to receiving and safely isolating media captured by client applications over local IPC.

## About

Sākṣī Vault exists to keep a separate, independent copy of important media captured by other apps (such as Sākṣī Camera). Instead of keeping the only copy inside the camera app, a client app can transparently send the captured media to Sākṣī Vault. This keeps the storage layer entirely separate from capture and viewing interfaces, reducing the risk of a single point of failure during pressured deletions.

## Features

- **Background-Only Execution**: Operates entirely in the background without any UI to minimize the surface area for compromise.
- **Media Transfer**: Receives photos, videos, and audio from client applications securely over Android Binder IPC (AIDL).
- **Secure Local Storage**: Stores received media privately on the device in a separate app container.
- **Metadata Management**: Stores file metadata and synchronization state using Room 3.
- **Scheduled Deletion**: Handles delayed deletion requests. Deletion requests immediately hide the media from connected apps but permanently remove the Vault copy only after a set delay (e.g., 30 days) using WorkManager and monotonic clock validation to resist simple time-tampering.
- **Offline Capable**: Requires no internet connection, keeping data local and private.

## Architecture / How It Works

Sākṣī Vault operates as an external service within the ecosystem:

1. **Client Apps (Camera/Portal)**: Initiate transfers and query media.
2. **Sākṣī SDK**: Provides the AIDL/IPC interface.
3. **Sākṣī Vault**: Exposes the `rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE` to securely receive IPC calls. It safely stores received media, manages incremental byte copying, and executes non-overlapping copy passes.

## Requirements / Compatibility

- Minimum Android Version: Android 10 (API Level 29)
- Language: Kotlin

## Security and Privacy

Vault provides a significant layer of security by decoupling storage from capture. However, because Vault’s unrestricted IPC API lets apps that bind to it schedule copies for deletion, the Vault does not independently preserve evidence if the capturing application (or another bound app) is completely compromised. Vault does not guarantee immunity from device wipe, physical destruction, or highly privileged (root) attacks.

## License

This project is licensed under the Apache License 2.0. See the LICENSE file for details.
