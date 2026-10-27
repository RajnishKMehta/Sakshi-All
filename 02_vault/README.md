# Sākṣī Vault

**Background-only storage for important media.**

Sākṣī Vault is the storage component of the Sākṣī Android ecosystem. It runs as a separate app without a normal user interface and receives supported media from compatible client apps through local Android IPC.

A separate Vault copy can help preserve important files if the visible copy is removed, but it is not an absolute guarantee against device loss, privileged access, app compromise, or other failure scenarios.

## Features

- Background-only media storage.
- Receives supported media through Android Binder/AIDL IPC.
- Keeps media in the app's private storage and maintains metadata and synchronization state.
- Supports incremental media copying where requested by compatible clients.
- Supports delayed deletion and recovery according to the implemented retention workflow.
- Uses local device storage; network access is not required for its core storage flow.

## How It Fits In

The Sākṣī SDK provides the IPC integration APIs. Compatible client apps such as [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera) can send media to Vault, while [Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal) provides the user-facing interface to browse and manage stored media.

## Requirements

- Android 10 (API 29) or newer, according to the project configuration.

## Build From Source

This project uses Gradle. From the project directory:

```bash
./gradlew assembleDebug
```

Run unit tests or code checks using the tasks configured in the repository when needed.

## License

See the repository's [LICENSE](LICENSE) file.
