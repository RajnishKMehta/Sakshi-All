# Sākṣī Vault

Sākṣī Vault is the secure, background-only storage application for the Sākṣī Android ecosystem.

It securely receives and protects media from client applications (such as Sākṣī Camera) over local IPC, keeping a separate, independent copy of important media beyond the ordinary gallery or file manager.

## Features

- **Background-Only Execution:** Operates entirely in the background without any user interface, reducing visibility and the surface area for compromise.
- **Independent Storage:** Stores a separate copy of media locally on the device, independent of the capture application.
- **Media Transfer & Sync:** Receives media, including incremental synchronization for fMP4 recordings, from client apps over Android IPC.
- **Metadata Management:** Manages file metadata and synchronization state using Room 3.
- **Scheduled Deletion:** Implements delayed deletion. Deletion requests hide the media immediately, but permanent deletion from the Vault occurs only after a set delay (30 days). This mechanism uses elapsed realtime (when boot metadata is valid) and falls back to wall-clock time to resist simple time-tampering.
- **Offline Capable:** Requires no internet connection; all operations and storage are entirely local.

## Architecture and Role

Sākṣī Vault is a core component of the Sākṣī ecosystem:

1. **[Sākṣī SDK](../01_sdk/README.md):** Provides the IPC interface to connect to the Vault.
2. **Sākṣī Vault (this repository):** Exposes a service (`rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE`) to securely receive IPC calls and stores media separately.
3. **Client Apps:** Capture apps like [Sākṣī Camera](../03_camera/README.md) and viewing apps like [Sākṣī Portal](../04_portal/README.md) communicate with the Vault via the SDK.

**Note:** The Vault is a separate background application, but it is not completely undetectable. Its presence can be discovered through system settings and app lists. Furthermore, it does not guarantee absolute protection against device seizure, factory resets, physical destruction, or privileged system attacks.

## Building from Source

To build Sākṣī Vault from source, use standard Gradle commands:

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
