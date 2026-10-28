# Sākṣī Vault

Sākṣī Vault is the secure, background-only storage application for the Sākṣī Android ecosystem. It receives and preserves an independent copy of media captured by client applications over local IPC.

## About

Sākṣī helps people retain important media and evidence by keeping a separate copy beyond the ordinary gallery or file manager. Sākṣī Vault acts as this separate storage layer. By operating entirely in the background without a conventional user interface, it minimizes its footprint while providing a dedicated space for preserving photos and videos.

Please note that the Vault is not an invisibility or anti-detection guarantee. It provides an additional layer of preservation, not absolute immunity from device-level attacks, privileged access, or discovery through system settings.

## Features

- **Background Operation:** Runs as an external service without a user interface to minimize exposure.
- **Incremental Receiving:** Continuously synchronizes Fragmented MP4 (fMP4) video segments from client apps while recording is ongoing.
- **Independent Storage:** Stores media in a separate app container, ensuring that deleting the visible original does not automatically destroy the Vault's copy.
- **Delayed Deletion:** Deletion requests immediately hide the media, but the Vault retains the actual files for 30 days before permanently deleting them, helping prevent forced, immediate erasure.
- **Time-Tampering Mitigation:** Uses the device's monotonic clock, where possible, to validate deletion eligibility, resisting simple wall-clock manipulation.
- **Local-Only:** Requires no internet connection, keeping your data entirely on the device.

## Architecture

Sākṣī Vault operates as a distinct service within the ecosystem:

1. **Client Apps (e.g., Sākṣī Camera):** Capture media.
2. **Sākṣī SDK:** Provides the IPC bridge.
3. **Sākṣī Vault (This Project):** Receives media via the `rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE` action, performs incremental byte copying, and manages the database of stored files using Room 3.
4. **Sākṣī Portal:** Connects to the Vault to allow users to view and export the preserved media.

## Documentation

For developers looking to integrate a client application with Sākṣī Vault, refer to the [Integration Guide](docs/INTEGRATION.md) and the documentation provided in the Sākṣī SDK repository.

## Build From Source

To build Sākṣī Vault locally:

```bash
./gradlew assembleDebug
```

## Security and Privacy

Sākṣī Vault is designed to provide a layer of preservation against forced deletion of the ordinary original copy. It does not guarantee protection against root-level attacks, device seizure, factory resets, or physical destruction. If the client app scheduling copies for deletion is compromised, the Vault cannot independently preserve the evidence, as it honors the API requests it receives.

## License

This project is licensed under the Apache License 2.0.
