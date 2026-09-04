# Sakshi SDK 📱⚡

[![Release](https://img.shields.io/maven-central/v/io.github.rajnishkmehta.sakshi/sakshi-sdk?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.github.rajnishkmehta.sakshi/sakshi-sdk)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg?style=plastic&logo=Android)](../../)
[![License](https://img.shields.io/github/license/RajnishKMehta/Sakshi-SDK?style=flat-square&logo=Apache)](https://github.com/RajnishKMehta/Sakshi-SDK/raw/refs/heads/main/LICENSE)

**Sakshi SDK** (`rajnishkmehta.sakshi.sdk`) is a lightweight, headless Android client and IPC library designed for local-first inter-process communication between trusted client applications (such as _Sakshi Camera_ or _Sakshi Audio_) and the **Sakshi Vault** application.

> [!WARNING]
> **Early Beta:** The SDK currently supports **photo functionality only**. Video and audio functionality are not implemented yet. APIs and behavior may change in future releases.

---

## 💡 What is Sakshi SDK?

Sakshi SDK provides clean, idiomatic, coroutine-powered Kotlin public APIs for:

1. **Client Developers (Camera, Audio, Viewer)**: Send photos, manage incremental audio/video sync, ping Vault, query recording status, and observe file copy completion acknowledgements (`CopyDoneAck`).
2. **Vault Developers**: Implement remote AIDL service binding and send structured responses, sync progress, copy completion acknowledgements, and error events using `VaultResponder`.

---

## 🏗️ Dual-Side Architecture & Responsibilities

```
+------------------------------------+                 +-----------------------------------+
|       Client Application           |                 |        Sakshi Vault App           |
|  (e.g., Camera / Audio / Viewer)   |                 |       (External Service)          |
|                                    |                 |                                   |
|  +------------------------------+  |   Android IPC   |  +-----------------------------+  |
|  |        SakshiClient          |  | <=============> |  |   Incremental Copy Engine   |  |
|  +------------------------------+  |   (AIDL/Binder) |  |   VaultResponder Helper     |  |
|  (Send Photos & Video Sync)     |                 |  |   Media Records Database    |  |
+------------------------------------+                 +-----------------------------------+
```

### SDK Responsibilities

- Inter-process communication bridge with Vault service.
- Hiding Binder proxy transactions, AIDL interfaces, and death recipient handling.
- Providing `SakshiClient` for client applications and `VaultResponder` for Vault applications.

### Vault Responsibilities (External App)

- File storage, incremental byte copying, and persistence.
- Interval timers, queue scheduling, and non-overlapping copy pass execution.
- Emitting `CopyDoneAck` (including File ID, optional original source URI, and total copied bytes) upon completing file transfers.

---

## 📦 Installation & Gradle Integration

Add the dependency to your app or library module's `build.gradle.kts`:

### Maven Central

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-beta.6")
}
```

### GitHub Packages

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/RajnishKMehta/sakshi-sdk")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
            password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-beta.6")
}
```

> **Note:** GitHub Packages requires authentication using a Personal Access Token (PAT), even for many public packages.

---

## 🚀 Quick Start Examples

### A. Client App Example (Camera / Audio)

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.models.*
import rajnishkmehta.sakshi.sdk.api.SakshiClient

val client = SakshiClient.create(context)

// 1. Send Photo (returns SakshiResult<CopyDoneAck>)
coroutineScope.launch {
    val result = client.sendPhoto(PhotoRequest(fileId = "photo_001", uri = photoUri))
    when (result) {
        is SakshiResult.Success -> {
            val ack = result.data
            println("Photo Sent! File ID: ${ack.fileId}, Copied Bytes: ${ack.totalCopiedBytes}")
        }
        is SakshiResult.Failure -> {
            println("Photo ingestion failed: ${result.error.message}")
        }
    }
}

// 2. Start AVSync (returns Flow<SakshiResult<AVSyncStatus>>)
coroutineScope.launch {
    client.startAVSync(AVSyncRequest(fileId = "rec_999", uri = avUri)).collect { result ->
        when (result) {
            is SakshiResult.Success -> {
                val status = result.data
                println("Sync State: ${status.state}, Copied Bytes: ${status.lastCopiedOffsetBytes}")
            }
            is SakshiResult.Failure -> {
                println("Sync Error: ${result.error.message}")
            }
        }
    }
}

// 3. Observe Copy Completion Acknowledgement (returns Flow<SakshiResult<CopyDoneAck>>)
coroutineScope.launch {
    client.observeCopyDone("rec_999").collect { result ->
        val ack = result.getOrNull()
        println("Copy Completed! File ID: ${ack?.fileId}, Original URI: ${ack?.originalUri}, Copied Bytes: ${ack?.totalCopiedBytes}")
    }
}
```

### B. Vault App Example (Service Side)

```kotlin
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import rajnishkmehta.sakshi.sdk.api.vault.VaultResponder
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultService

class SakshiVaultRemoteService : Service() {
    private val binder = object : ISakshiVaultService.Stub() {
        override fun startAVSync(avSyncBundle: Bundle, callback: ISakshiVaultCallback) {
            val fileId = avSyncBundle.getString("file_id", "")
            val sourceUriStr = avSyncBundle.getString("uri", "")

            // Vault copies bytes incrementally...
            val totalCopied = performVaultCopy(fileId)

            // Vault sends CopyDoneAck back to Client app
            VaultResponder.sendCopyDone(
                callback,
                CopyDoneAck(
                    fileId = fileId,
                    originalUri = if (sourceUriStr.isNullOrEmpty()) null else Uri.parse(sourceUriStr),
                    totalCopiedBytes = totalCopied
                )
            )
        }
        // ...
    }
    override fun onBind(intent: Intent?): IBinder = binder
}
```

---

## 📄 Documentation

Detailed technical specifications are available in the [`/docs`](docs) directory:

- 📑 [System Architecture](docs/architecture.adoc)
- 📑 [Client API Usage](docs/api_usage.adoc)
- 📑 [Vault Integration Guide](docs/vault_integration.adoc)
- 📑 [AIDL IPC Specification](docs/ipc_specification.adoc)
- 📑 [Development Guide](docs/development.adoc)

---

## 📄 License

This project is licensed under the [![LICENSE](https://img.shields.io/github/license/RajnishKMehta/Sakshi-SDK?logo=apache&label=%20&color=7C297D)](LICENSE).
