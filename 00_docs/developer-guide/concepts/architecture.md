# Architecture & Concepts

The Sākṣī ecosystem is composed of several independent Android applications and a client library that communicate using an Inter-Process Communication (IPC) architecture.

## Ecosystem Architecture

Sākṣī relies on four core components:

1.  **Vault** ([Sakshi-Vault](https://github.com/RajnishKMehta/Sakshi-Vault)): A background-only secure storage application with no user interface. Its responsibilities include securely persisting media files, scheduling synchronization, and managing file lifecycles such as deletion and recovery. It acts as the IPC Server.
2.  **SDK** ([Sakshi-SDK](https://github.com/RajnishKMehta/Sakshi-SDK)): A headless Android IPC client library (`rajnishkmehta.sakshi.sdk`). It encapsulates raw AIDL communication and exposes a clean, coroutine-based Kotlin interface (`SakshiClient`) to client applications.
3.  **Camera** ([Sakshi-Camera](https://github.com/RajnishKMehta/Sakshi-Camera)): A client application using CameraX and Media3 to capture and stream fragmented MP4 (fMP4) data directly to the Vault through the SDK.
4.  **Portal** ([Sakshi-Portal](https://github.com/RajnishKMehta/Sakshi-Portal)): A user-facing Jetpack Compose client application. It uses the SDK to query the Vault for stored media, allowing users to view, delete, and recover files.

All core Sākṣī Android modules strictly enforce a minimum SDK version of API 29 (Android 10).

## IPC/AIDL Communication Architecture

Client applications (Camera, Portal) never directly access the Vault's database or internal files. Instead, they communicate exclusively via the `SakshiClient` interface provided by the SDK.

Under the hood, the `SakshiClient` (specifically `SakshiClientImpl`) binds to the Vault's `SakshiVaultRemoteService` using an Android `ServiceConnection`. The Vault exposes its functionality via the `ISakshiVaultService` AIDL interface.

### The Flow:
1.  **Client Request**: A client application calls a `suspend` function on `SakshiClient` (e.g., `listMedia()`).
2.  **Service Binding**: The SDK ensures a valid, active connection to the Vault service.
3.  **IPC Call**: The SDK executes the corresponding AIDL method via `withContext(Dispatchers.IO)`.
4.  **Vault Processing**: Inside the Vault application, `SakshiVaultServiceBinder` receives the request. The Vault schedules the work within its own `CoroutineScope` or uses Room database `runBlocking` calls depending on the operation.
5.  **Response Delivery**: The Vault returns an Android `Bundle` containing the results or error information.
6.  **Client Translation**: The SDK unpacks the `Bundle` and returns a strongly-typed `SakshiResult.Success` or `SakshiResult.Failure`.

This architecture ensures process isolation and centralizes media lifecycle management within the Vault application.
