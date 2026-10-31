# IPC & Application Security

Sākṣī relies on local Inter-Process Communication (IPC) for applications to interact with the Vault. Sākṣī Camera and Sākṣī Portal use the **Sākṣī SDK** to securely send commands and transfer data to Sākṣī Vault.

## Local Communication Only

All communication between Sākṣī components occurs entirely on the local device using standard Android mechanisms. **Sākṣī does not use network sockets, local web servers, or internet connections for IPC.**

## Binder and AIDL Interfaces

The Sākṣī SDK utilizes Android Interface Definition Language (AIDL) and the Android Binder framework to communicate with the Vault service.

*   **Service Exposure:** Vault exposes a bound service that client apps connect to.
*   **Path Validation:** The SDK and Vault include strict path validation logic (`PathValidator`) to ensure that file IDs and requested paths do not contain directory traversal sequences (e.g., `../`). This prevents a malicious client from attempting to read or overwrite arbitrary files outside the designated media directories within Vault's storage.

## Trust Boundaries and Permissions

*   **SDK as a Bridge:** The SDK acts purely as a headless client library. It hides the complexity of AIDL transactions and proxy death handling, but it does not implement its own security restrictions against the host application. If an application can instantiate the SDK, it can communicate with the Vault.
*   **Vault Application:** Vault currently accepts connections from local client applications. The primary security boundary is Android's sandboxing.
*   **No Implicit Internet Access:** Because the core communication is handled via Binder, the SDK and Vault do not require the `INTERNET` permission to function. (Note: Portal may use networking libraries for local image loading tasks, such as Coil, but this is not used for IPC or data exfiltration).

For detailed integration instructions regarding the SDK and IPC, see the [SDK Integration Guide](../developer-guide/sdk/integration.md).
