# SDK API Reference

This section provides comprehensive documentation for all public methods exposed by the `SakshiClient` interface.

The `SakshiClient` is the primary entry point for any client application communicating with the Vault. These APIs allow developers to perform health checks, synchronize media, query catalogs, and manage the lifecycle of stored files.

Each API page details the method's purpose, signature, expected behaviour, and how to integrate it on the client side, along with an explanation of how the Vault processes the request internally. For an overview of the Vault's common handling patterns (like asynchronous responses and IPC error serialization), please start by reading the Shared Vault Integration page.

*   [Shared Vault Integration](vault-integration.md)
* [pingVault](pingVault.md) - Performs a health check to verify Vault connection and service availability.
* [copyFile](copyFile.md) - Sends a file payload to Vault for ingestion.
* [startAVSync](startAVSync.md) - Initiates real-time audio/video synchronization for a recording in Vault.
* [pauseAVSync](pauseAVSync.md) - Temporarily pauses audio/video synchronization for a specific file.
* [resumeAVSync](resumeAVSync.md) - Resumes a paused audio/video synchronization process.
* [stopAVSync](stopAVSync.md) - Stops audio/video synchronization for a specific file.
* [isAVSynced](isAVSynced.md) - Checks whether a specific recording exists or is currently syncing in Vault.
* [listMedia](listMedia.md) - Retrieves a JSON catalog of all stored media files, excluding those scheduled for deletion.
* [listDeletedMedia](listDeletedMedia.md) - Retrieves a JSON catalog of all media files currently scheduled for deletion.
* [getMedia](getMedia.md) - Retrieves the details and ContentResolver URI of a specific media file.
* [getThumbnail](getThumbnail.md) - Retrieves the ContentProvider URI template for accessing media thumbnails.
* [deleteMedia](deleteMedia.md) - Schedules one or multiple media files for permanent deletion after a retention period.
* [recoverMedia](recoverMedia.md) - Restores one or multiple media files previously scheduled for deletion back to a valid state.
* [disconnect](disconnect.md) - Explicitly disconnects from the Vault IPC service and releases internal resources.
