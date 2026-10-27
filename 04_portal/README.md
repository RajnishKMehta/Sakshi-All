# Sākṣī Portal

**Browse and manage media stored in Sākṣī Vault.**

Sākṣī Portal is the user-facing Android app in the Sākṣī ecosystem. It connects to Vault through the Sākṣī SDK and provides an interface for accessing the media stored there.

## Features

- Browse media stored in Vault.
- View photos and play supported video and audio files.
- Export media to supported device storage locations.
- Browse media scheduled for deletion and recover eligible items, where supported by the current implementation.
- App lock and configurable authentication options, as implemented in the app.
- Jetpack Compose-based user interface.

## How It Fits In

Portal uses the [Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK) to communicate with [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault). [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera) can capture media and send it to Vault.

Portal does not directly access Vault's private filesystem or database.

## Requirements

- Android 10 (API 29) or newer, according to the project configuration.

## Build From Source

From the project directory:

```bash
./gradlew assembleDebug
```

## License

See the repository's [LICENSE](LICENSE) file.
