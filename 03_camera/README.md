# Sākṣī Camera

**Capture media independently or with Sākṣī Vault integration.**

Sākṣī Camera is an Android camera app designed to work on its own and optionally integrate with the Sākṣī ecosystem. With Vault integration configured, it can transfer supported media to a separate Vault copy through the Sākṣī SDK.

## Features

- Photo and video capture.
- Fragmented MP4 (fMP4) recording for improved resilience when recording is interrupted.
- Incremental synchronization of supported fMP4 recordings to Vault while recording.
- Standard MP4 recording, transferred to Vault after recording finishes.
- Android Views/XML-based UI, according to the current implementation.

## Recording and Synchronization

- **fMP4:** The fragmented format supports incremental synchronization while recording and is designed to improve recoverability after an unexpected interruption.
- **Standard MP4:** The recording is copied to Vault only after recording finishes. It is not equivalent to the crash-resilience behavior of fMP4, and cannot be incrementally synchronized through the current flow.

Successful transfer to Vault can provide a separate copy, but it does not guarantee preservation under every failure or compromise scenario.

## Requirements

- Android 10 (API 29) or newer, according to the project configuration.

## Integration

Camera uses the [Sākṣī SDK](https://github.com/RajnishKMehta/Sakshi-SDK) to communicate with [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault). The camera can also be used independently of the ecosystem.

## Build From Source

From the project directory:

```bash
./gradlew assembleDebug
```

## License

See the repository's [LICENSE](LICENSE) file.
