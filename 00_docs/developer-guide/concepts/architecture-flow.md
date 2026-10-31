# Architecture Flow

This flowchart illustrates the detailed sequence of interactions between client applications, the Sākṣī SDK, and the Vault service during a typical operation. It outlines the responsibilities of each component and the specific Android and IPC mechanisms involved.

```mermaid
flowchart TD
    %% Definitions
    subgraph ClientLayer ["1. Client Application Layer (Camera / Portal)"]
        ClientReq(["Client Request\n(e.g., suspend fun listMedia)"])
        ClientRes(["SakshiResult\n(Success / Failure)"])
    end

    subgraph SDKLayer ["2. Sākṣī SDK (rajnishkmehta.sakshi.sdk)"]
        SakshiClient["SakshiClientImpl"]
        CheckConn{"VaultServiceConnection\nIs Service Connected?"}
        BindService["Context.bindService()\n(Intent to Vault)"]
        DispatchIO["withContext(Dispatchers.IO)"]
        Unpack["Unpack Android Bundle\n& Map to Data Models"]
    end

    subgraph OS ["3. Android OS (IPC)"]
        BinderReq[("Android Binder\n(ISakshiVaultService.aidl)")]
        BinderRes[("Android Binder\n(Bundle Callback)")]
    end

    subgraph VaultLayer ["4. Vault Service Layer (Sakshi-Vault)"]
        VaultBinder["SakshiVaultServiceBinder\n(Receives AIDL call)"]
        Validate{"Validation &\nPathValidator"}
        Processing["CoroutineScope /\nRoom runBlocking"]
        PackBundle["Pack Result/Error\ninto Bundle"]
    end

    subgraph StorageLayer ["5. Vault Core Storage Layer"]
        DB[("Room 3 Database\n(MediaRecord)")]
        FileStore[("Vault Internal\nFile Storage")]
        WorkManager["WorkManager\n(Sync/Deletion)"]
    end

    %% Flow - Request Phase
    ClientReq -->|1a. Calls SDK| SakshiClient
    SakshiClient -->|1b. Delegates to| CheckConn
    CheckConn -- "No" --> BindService
    BindService --> |"Waits for ServiceConnection"| CheckConn
    CheckConn -- "Yes" --> DispatchIO

    DispatchIO -->|2. AIDL Method Call| BinderReq
    BinderReq -->|3. Cross-process Request| VaultBinder

    %% Flow - Vault Processing Phase
    VaultBinder --> Validate
    Validate -- "Valid" --> Processing
    Validate -- "Invalid" --> PackBundle

    Processing <-->|4a. Query / Update| DB
    Processing <-->|4b. I/O Operations| FileStore
    Processing -.->|4c. Schedule Background Tasks| WorkManager

    Processing -->|4d. Result Ready| PackBundle

    %% Flow - Response Phase
    PackBundle -->|5. Return Bundle| BinderRes
    BinderRes -->|6. Cross-process Response| Unpack
    Unpack -->|7. Map to SDK Models| ClientRes

    %% Styling classes
    classDef client fill:#e3f2fd,stroke:#1565c0,stroke-width:2px
    classDef sdk fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px
    classDef os fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef vault fill:#f3e5f5,stroke:#6a1b9a,stroke-width:2px
    classDef storage fill:#fffde7,stroke:#fbc02d,stroke-width:2px
    classDef check fill:#ffebee,stroke:#c62828,stroke-width:2px

    %% Apply styles
    class ClientReq,ClientRes client
    class SakshiClient,BindService,DispatchIO,Unpack sdk
    class CheckConn check
    class BinderReq,BinderRes os
    class VaultBinder,Validate,Processing,PackBundle vault
    class DB,FileStore,WorkManager storage
```

## Key Interactions

1. **Client Request**: The client application (like Portal or Camera) calls a high-level `suspend` function on `SakshiClient`.
2. **Connection Management**: The SDK's `VaultServiceConnection` checks if it's already bound to the Vault. If not, it uses Android's `bindService` to establish the connection before proceeding.
3. **Cross-Process Execution**: The SDK moves the actual AIDL execution to the `Dispatchers.IO` thread to avoid blocking the client's main thread during the IPC transaction.
4. **Vault Processing**: Once `SakshiVaultServiceBinder` receives the request inside the Vault process, it validates the inputs. It then performs the necessary operations using Room (database) and local file storage, either synchronously (`runBlocking` for fast database reads) or asynchronously (`CoroutineScope` for I/O).
5. **Response Translation**: The result from the Vault is packed into an Android `Bundle` and sent back over the Binder. The SDK unpacks this, mapping it into a clean `SakshiResult.Success` or `SakshiResult.Failure` for the client to consume natively.
