# Sakshi Integration & Developer Guide

Welcome to the Sakshi integration documentation!

If you want to integrate a Client Application (such as a Camera app, Audio Recorder, or Viewer) with the **Sakshi Vault**, you need to use the **Sakshi SDK**.

The Sakshi SDK handles all the heavy lifting of Inter-Process Communication (IPC) via Android AIDL.

## Permissions Setup

For a client application to successfully communicate with the Sakshi Vault service, it must declare the required custom permission in its `AndroidManifest.xml` file.


Add the following to your client app's `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.your.client.app">

    <!------------------------------->

    <!-- Required on Android 11+ (API 30+) for Vault integration
         to discover installed Vault applications. Use either Option 1
         or Option 2, depending on the required package visibility. -->

    <!-- Option 1: Broad package visibility -->
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />

    <!-- OR -->

    <!-- Option 2: Visibility limited to the specific Vault package -->
    <queries>
        <package android:name="rajnishkmehta.sakshi.vault" />
    </queries>

    <!------------------------------->

    <application>
        ...
    </application>
</manifest>
```

## Official Documentation

Rather than duplicating the comprehensive guides that live in the Sakshi SDK repository, please refer directly to the official up-to-date documentation links below for detailed integration instructions:

### 1. IPC Specification
Understand the underlying AIDL interface contracts, service binding configuration, and the bundle data structures sent between the client and the Vault.
* 👉 [Sakshi SDK AIDL IPC Specification](https://github.com/RajnishKMehta/Sakshi-SDK/blob/main/docs/ipc_specification.adoc)

### 2. Client API Usage
If you are developing a **Client App** that captures media and you want to send photos or stream videos to the Vault, start here. This guide explains how to initialize the SDK, submit photos, start video sync, and query status.
* 👉 [Sakshi SDK Client API Usage Guide](https://github.com/RajnishKMehta/Sakshi-SDK/blob/main/docs/api_usage.adoc)

### 3. Vault Integration
If you want to understand the inner workings of how the **Vault** responds to these requests, or if you are contributing directly to this Vault repository, this guide details how the `ISakshiVaultService.Stub` is implemented and how `VaultResponder` is used.
* 👉 [Sakshi SDK Vault Integration Guide](https://github.com/RajnishKMehta/Sakshi-SDK/blob/main/docs/vault_integration.adoc)

---

## Quick Summary: How it works

1. **Client App**: Integrates the Sakshi SDK client library.
2. **Binding**: The SDK binds to the Vault using the action `rajnishkmehta.sakshi.vault.BIND_VAULT_SERVICE`.
3. **Data Transfer**:
   - **Photos**: Client sends a `PhotoRequest` bundle; Vault copies the file and returns a `CopyDoneAck`.
   - **Videos**: Client sends a `VideoSyncRequest`; Vault performs continuous incremental copying and streams `VideoSyncStatus` updates until completion.
4. **Resiliency**: The Vault Service acts as a background boundary. Even if the Client App is forcefully closed or the original file deleted, the copied data remains safely in the Vault's storage sandbox.
