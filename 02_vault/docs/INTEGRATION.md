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

    <!-- =========================================================== -->

    <!-- Required on Android 11+ (API 30+) for Vault integration
         to discover installed Vault applications. Use either
         Option 1 or Option 2, depending on the required
         package visibility. -->

    <!-- Option 1: Broad package visibility -->
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />

    <!-- OR -->

    <!-- Option 2: Visibility limited to the specific Vault package -->
    <queries>
        <package android:name="rajnishkmehta.sakshi.vault" />
    </queries>

    <!-- =========================================================== -->

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
   - **Photos**: Client sends a `FileCopyRequest` bundle; Vault copies the file and returns a `CopyDoneAck`.
   - **Videos**: Client sends a `VideoSyncRequest`; Vault performs continuous incremental copying and streams `VideoSyncStatus` updates until completion.
4. **Resiliency**: The Vault Service acts as a background boundary. Even if the Client App is forcefully closed or the original file deleted, the copied data remains safely in the Vault's storage sandbox.

## Scheduled Media Deletion

`SakshiClient.deleteMedia(fileId)` and `SakshiClient.deleteMedia(fileIds)` **schedule** deletion; they never delete immediately.

1. **Scheduling**: Vault validates each file ID and atomically moves the record from `COMPLETED`, `FAILED`, or `STOPPED` to the `DELETE` state, storing `deletionScheduledAt` (plus the monotonic clock and boot count captured at the same time). Unknown IDs and files still being synchronized (`INITIALIZING`, `SYNCING`, `PAUSED`, or an active sync job) are rejected. Repeating a request for an already scheduled file succeeds and keeps the original timestamp. Batch requests report per-file results in `successfulIds` and `failedIds`.
2. **Visibility**: Records in the `DELETE` state are excluded from `listMedia` immediately. Other APIs do not filter them: `getMedia` still returns the record and `isAVSynced` reports `sync_state = DELETE` until cleanup removes it.
3. **Copy protection**: Photo copies, sync passes, deletion scheduling, and cleanup share one per-file lock, and copies never overwrite a `DELETE` record.
4. **Permanent cleanup**: After at least 30 full days, Vault deletes its private media file and thumbnail, then removes the database record. If any file cannot be deleted, the record stays in `DELETE` and cleanup is retried; already-missing files are treated as deleted. Public exported copies and original camera files are never touched.
5. **Triggers**: WorkManager is the primary mechanism (a unique one-time request per file plus a daily unique periodic sweep, battery-not-low constraint, exponential backoff). Every Vault `ping` and every Vault service start also run a non-blocking fallback sweep. All triggers share process-wide locks, so duplicate or simultaneous runs are safe.

**Clock limitation**: While the device has not rebooted since scheduling, eligibility uses the monotonic `elapsedRealtime` clock, so wall-clock changes have no effect. After a reboot only the wall clock is available: moving it backwards delays deletion, and moving it forwards on an offline device can shorten the effective retention period. This cannot be fully prevented without a trusted time source.

**Database schema**: The media record field `completionState` was renamed to `state`. Beta builds use a fresh schema without migrations; uninstall the previous beta before installing a new one.
