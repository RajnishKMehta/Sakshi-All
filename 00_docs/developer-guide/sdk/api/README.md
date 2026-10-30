# SDK API Reference

This section provides comprehensive documentation for all public methods exposed by the `SakshiClient` interface.

The `SakshiClient` is the primary entry point for any client application communicating with the Vault. These APIs allow developers to perform health checks, synchronize media, query catalogs, and manage the lifecycle of stored files.

Each API page details the method's purpose, signature, expected behaviour, and how to integrate it on the client side, along with an explanation of how the Vault processes the request internally. For an overview of the Vault's common handling patterns (like asynchronous responses and IPC error serialization), please start by reading the Shared Vault Integration page.

*   [Shared Vault Integration](vault-integration.md)
*   [pingVault](pingVault.md)
*   [copyFile](copyFile.md)
*   [startAVSync](startAVSync.md)
*   [pauseAVSync](pauseAVSync.md)
*   [resumeAVSync](resumeAVSync.md)
*   [stopAVSync](stopAVSync.md)
*   [isAVSynced](isAVSynced.md)
*   [listMedia](listMedia.md)
*   [listDeletedMedia](listDeletedMedia.md)
*   [getMedia](getMedia.md)
*   [getThumbnail](getThumbnail.md)
*   [deleteMedia](deleteMedia.md)
*   [recoverMedia](recoverMedia.md)
*   [disconnect](disconnect.md)
