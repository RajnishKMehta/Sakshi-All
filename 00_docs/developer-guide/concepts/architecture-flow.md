# Architecture Flow

This flowchart illustrates the detailed sequence of interactions between client applications, the Sākṣī SDK, and the Vault service during a typical operation. It outlines the responsibilities of each component and the specific Android and IPC mechanisms involved.

```mermaid
---
config:
  layout: elk
  theme: base
  look: classic
  fontFamily: "Inter, Arial, sans-serif"
  htmlLabels: false

  themeVariables:
    background: "#F8FAFC"
    primaryColor: "#EEF2FF"
    primaryTextColor: "#312E81"
    primaryBorderColor: "#6366F1"
    secondaryColor: "#CCFBF1"
    secondaryTextColor: "#134E4A"
    tertiaryColor: "#DBEAFE"
    lineColor: "#64748B"
    textColor: "#1E293B"
    edgeLabelBackground: "#FFFFFF"
    clusterBkg: "#FFFFFF"
    clusterBorder: "#CBD5E1"
    nodeTextColor: "#1E293B"
    defaultLinkColor: "#64748B"
    fontSize: "14px"

  flowchart:
    nodeSpacing: 55
    rankSpacing: 75
    diagramPadding: 24
    curve: basis
    inheritDir: true

---
flowchart TD

    %% 1. CLIENT APPLICATION LAYER
    subgraph ClientLayer["01 · CLIENT APPLICATION LAYER"]
        direction LR
        ClientReq(["Client Request<br/>(e.g. suspend fun listMedia)"])
        ClientRes(["SakshiResult<br/>(Success / Failure)"])
    end

    %% 2. SDK LAYER
    subgraph SDKLayer["02 · SĀKṢĪ SDK"]
        direction TB
        SakshiClient["SakshiClientImpl"]
        CheckConn{"Vault Service<br/>Connected?"}
        BindService["bindService()<br/>Connect to Vault"]
        DispatchIO["withContext<br/>(Dispatchers.IO)"]
        Unpack["Unpack Bundle<br/>Map to Data Models"]
    end

    %% 3. ANDROID IPC LAYER
    subgraph OS["03 · ANDROID IPC LAYER"]
        direction LR
        BinderReq[("AIDL Request<br/>ISakshiVaultService")]
        BinderRes[("Bundle Callback<br/>Response")]
    end

    %% 4. VAULT SERVICE LAYER
    subgraph VaultLayer["04 · VAULT SERVICE LAYER"]
        direction TB
        VaultBinder["SakshiVaultServiceBinder"]
        Validate{"Validation<br/>& PathValidator"}
        Processing["CoroutineScope<br/>Request Processing"]
        PackBundle["Build Response Bundle<br/>Result / Error"]
    end

    %% 5. CORE STORAGE LAYER
    subgraph StorageLayer["05 · VAULT CORE STORAGE"]
        direction LR
        DB[("Room 3 Database<br/>MediaRecord")]
        FileStore[("Private File Storage")]
        WorkManager["WorkManager<br/>Background Tasks"]
    end

    %% REQUEST FLOW
    ClientReq -->|"1 · SDK API"| SakshiClient
    SakshiClient -->|"2 · Check connection"| CheckConn

    CheckConn -->|"No"| BindService
    BindService -->|"Service Connected"| CheckConn
    CheckConn -->|"Yes"| DispatchIO

    DispatchIO -->|"3 · AIDL Method Call"| BinderReq
    BinderReq -->|"4 · IPC Request"| VaultBinder

    %% VAULT PROCESSING
    VaultBinder --> Validate

    Validate -->|"Valid"| Processing
    Validate -->|"Invalid"| PackBundle

    Processing <-->|"4a · Query / Update"| DB
    Processing <-->|"4b · Read / Write"| FileStore
    Processing -.->|"4c · Schedule Tasks"| WorkManager

    Processing -->|"5 · Processing Complete"| PackBundle

    %% RESPONSE FLOW
    PackBundle -->|"6 · Return Bundle"| BinderRes
    BinderRes -->|"7 · IPC Response"| Unpack
    Unpack -->|"8 · SDK Model Mapping"| ClientRes

    %% NODE STYLES
    classDef client fill:#EEF2FF,stroke:#6366F1,stroke-width:2px,color:#312E81
    classDef sdk fill:#E0E7FF,stroke:#4F46E5,stroke-width:2px,color:#312E81
    classDef sdkCore fill:#4338CA,stroke:#312E81,stroke-width:3px,color:#FFFFFF
    classDef decision fill:#FEF3C7,stroke:#D97706,stroke-width:2px,color:#92400E
    classDef os fill:#FFF7ED,stroke:#EA580C,stroke-width:2px,color:#9A3412
    classDef vault fill:#CCFBF1,stroke:#0D9488,stroke-width:2px,color:#134E4A
    classDef storage fill:#DBEAFE,stroke:#3B82F6,stroke-width:2px,color:#1E3A8A
    classDef background fill:#DCFCE7,stroke:#16A34A,stroke-width:2px,color:#14532D

    class ClientReq,ClientRes client
    class SakshiClient sdkCore
    class BindService,DispatchIO,Unpack sdk
    class CheckConn,Validate decision
    class BinderReq,BinderRes os
    class VaultBinder,Processing,PackBundle vault
    class DB,FileStore storage
    class WorkManager background

    %% LAYER STYLES
    style ClientLayer fill:#F8FAFF,stroke:#A5B4FC,stroke-width:1.5px,color:#3730A3
    style SDKLayer fill:#F5F3FF,stroke:#C4B5FD,stroke-width:1.5px,color:#5B21B6
    style OS fill:#FFF7ED,stroke:#FDBA74,stroke-width:1.5px,color:#9A3412
    style VaultLayer fill:#F0FDFA,stroke:#99F6E4,stroke-width:1.5px,color:#115E59
    style StorageLayer fill:#EFF6FF,stroke:#93C5FD,stroke-width:1.5px,color:#1E40AF
```

## Key Interactions

1. **Client Request**: The client application (like Portal or Camera) calls a high-level `suspend` function on [`SakshiClient`](../../developer-reference/api/README.md).
2. **Connection Management**: The SDK's `VaultServiceConnection` checks if it's already bound to the Vault. If not, it uses Android's `bindService` to establish the connection before proceeding.
3. **Cross-Process Execution**: The SDK moves the actual AIDL execution to the `Dispatchers.IO` thread to avoid blocking the client's main thread during the IPC transaction.
4. **Vault Processing**: Once `SakshiVaultServiceBinder` receives the request inside the Vault process, it validates the inputs. It then performs the necessary operations using Room (database) and local file storage, either synchronously (`runBlocking` for fast database reads) or asynchronously (`CoroutineScope` for I/O).
5. **Response Translation**: The result from the Vault is packed into an Android `Bundle` and sent back over the Binder. The SDK unpacks this, mapping it into a clean [`SakshiResult.Success`](../../developer-reference/data-models.md#sakshiresultt) or [`SakshiResult.Failure`](../../developer-reference/data-models.md#sakshiresultt) for the client to consume natively.
