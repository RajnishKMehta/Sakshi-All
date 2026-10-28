# Sākṣī Camera

Sākṣī Camera is the primary capture application for the Sākṣī ecosystem. It looks and functions like a normal camera app, but integrates with Sākṣī Vault to securely copy recordings.

## About

Sākṣī helps people retain important media and evidence by keeping a separate copy beyond the ordinary gallery or file manager. Sākṣī Camera is the starting point: it captures photos and videos and uses the Sākṣī SDK to securely transfer them over local IPC to the independent Sākṣī Vault.

By separating capture from storage, deleting a recording in the Camera app does not automatically delete the completed Vault copy. Sākṣī Camera can be used entirely independently or connected to the ecosystem.

## Features

- **Photo and Video Capture:** Standard camera functionalities built using modern Jetpack CameraX and XML layouts.
- **Interruption-Resilient fMP4 Recording:** Supports Fragmented MP4 (fMP4) natively using AndroidX Media3. This format writes data in fragments, making the recording crash-safe and allowing partial recovery if the process is unexpectedly interrupted.
- **Incremental Synchronization:** For fMP4 recordings, the camera streams the data incrementally to Sākṣī Vault *while the recording is still ongoing*.
- **Post-Recording Copy:** For standard MP4 recordings, the camera copies the complete file to the Vault only after the recording finishes.
- **Local-Only:** Requires no internet connection, keeping data local and private.

## Architecture

Sākṣī Camera utilizes two different recording pipelines depending on the chosen format:

- **fMP4 (Default):** Uses a custom pipeline integrating Media3's `FragmentedMp4Muxer` with CameraX. This allows the Sākṣī SDK to incrementally synchronize the ongoing recording to the Vault.
- **Normal MP4:** Uses the standard CameraX platform muxer. Because normal MP4 is not crash-safe and its structure is finalized at the end, synchronization to the Vault only occurs after the entire recording is completed.

**Ecosystem Flow:**
1. Sākṣī Camera captures media.
2. Sākṣī SDK handles the local IPC communication.
3. Sākṣī Vault safely stores the independent copy in the background.

## Build From Source

To build Sākṣī Camera locally:

```bash
./gradlew assembleDebug
```

## Security and Privacy

Sākṣī Camera provides an additional layer of preservation by copying media to the Vault. It does not provide absolute immunity against device-level attacks, seizure, or physical destruction. Because the Vault honors deletion schedules sent via the SDK's open IPC API, if the Camera app (or another bound client) is compromised, the Vault cannot independently preserve the evidence against malicious deletion requests.

## License

This project is licensed under the Apache License 2.0.
