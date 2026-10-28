<div id="top" align="center">

# Sākṣī SDK (साक्षी SDK)

[![Release](https://img.shields.io/maven-central/v/io.github.rajnishkmehta.sakshi/sakshi-sdk?logo=apache-maven&style=flat-square)](https://central.sonatype.com/artifact/io.github.rajnishkmehta.sakshi/sakshi-sdk)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg?style=plastic&logo=Android)](https://github.com/RajnishKMehta/Sakshi-SDK)
[![License](https://img.shields.io/github/license/RajnishKMehta/Sakshi-SDK?style=flat-square&logo=Apache)](https://github.com/RajnishKMehta/Sakshi-SDK/raw/refs/heads/main/LICENSE)
[![Docs](https://img.shields.io/badge/Docs-Sākṣī_Documentation-blue.svg?logo=gitbook)](https://github.com/RajnishKMehta/Sakshi-Docs)

</div>

**Sākṣī SDK** (`rajnishkmehta.sakshi.sdk`) is a lightweight, headless Android client and IPC library designed for local-first inter-process communication between trusted client applications (such as [Sākṣī Camera](https://github.com/RajnishKMehta/Sakshi-Camera) or [Sākṣī Portal](https://github.com/RajnishKMehta/Sakshi-Portal)) and the [Sākṣī Vault](https://github.com/RajnishKMehta/Sakshi-Vault) application.
> Sākṣī ≈ Sakshi

---

## Architecture & Responsibilities

```text
+------------------------------------+                 +-----------------------------------+
|       Client Application           |                 |        Sākṣī Vault App            |
|  (e.g., Camera / Portal)           |                 |       (External Service)          |
|                                    |                 |                                   |
|  +------------------------------+  |  +-----------+  |  +-----------------------------+  |
|  |        SakshiClient          |  |<=| Sākṣī SDK |=>|  |   Incremental Copy Engine   |  |
|  +------------------------------+  |  +-----------+  |  |   VaultResponder Helper     |  |
|  (Copy Files & Video Sync)         |  (AIDL IPC)     |  |   Media Records Database    |  |
+------------------------------------+                 +-----------------------------------+
```

### SDK Responsibilities

- Inter-process communication bridge with Vault service.
- Hiding Binder proxy transactions, AIDL interfaces, and death recipient handling.
- Providing `SakshiClient` for client applications and `VaultResponder` for Vault applications.
- **Strictly Headless**: The SDK contains no UI components (Activities, Fragments, Compose, or XML layouts).

---

## Installation

Add the dependency to your app or library module's `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.rajnishkmehta.sakshi:sakshi-sdk:1.0.0-RC.3")
}
```

> **Note**: A GitHub Packages distribution is also available. See the [Sākṣī Documentation](https://github.com/RajnishKMehta/Sakshi-Docs) for alternative setup instructions.

---

## Quick Start Examples

### A. Client App Example (Camera / Portal)

```kotlin
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.models.*

val client = SakshiClient.create(context)

// 1. Copy File (returns SakshiResult<CopyDoneAck>)
coroutineScope.launch {
    val result = client.copyFile(FileCopyRequest(fileId = "photo_001", uri = photoUri, mediaType = "PHOTO", fileExtension = "jpg"))
    when (result) {
        is SakshiResult.Success -> {
            val ack = result.data
            println("File Copied! File ID: ${ack.fileId}, Copied Bytes: ${ack.totalCopiedBytes}")
        }
        is SakshiResult.Failure -> {
            println("File ingestion failed: ${result.error.message}")
        }
    }
}

// 2. Start AVSync (returns Flow<SakshiResult<AVSyncStatus>>)
coroutineScope.launch {
    client.startAVSync(AVSyncRequest(fileId = "rec_999", uri = avUri, mediaType = "VIDEO", fileExtension = "mp4")).collect { result ->
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

// 3. Pause & Resume AVSync
coroutineScope.launch {
    client.pauseAVSync("rec_999") // Pauses ongoing sync
    // ... later ...
    client.resumeAVSync("rec_999") // Resumes sync
}

// 4. Stop AVSync and Receive Copy Completion Acknowledgement
coroutineScope.launch {
    val result: SakshiResult<CopyDoneAck> = client.stopAVSync("rec_999")
    when (result) {
        is SakshiResult.Success -> {
            val ack = result.data
            println("Copy Completed! File ID: ${ack.fileId}")
        }
        is SakshiResult.Failure -> {
            println("Stop AVSync error: ${result.error.message}")
        }
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

class SakshiVaultServiceBinder : ISakshiVaultService.Stub() {

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
}
```

---

## Documentation

Detailed technical specifications and integration guides are available in the [Sākṣī Documentation Repository](https://github.com/RajnishKMehta/Sakshi-Docs).

---

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before contributing.

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
