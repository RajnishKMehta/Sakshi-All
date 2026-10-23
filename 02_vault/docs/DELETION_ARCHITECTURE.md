# Vault Deletion Architecture

This document explains the lifecycle of media deletion within the Sakshi Vault, detailing how deletion requests are scheduled, managed, evaluated, and permanently executed.

## 1. Triggering a Deletion Request

The deletion process begins when the Vault receives an Inter-Process Communication (IPC) request from the SDK (e.g., via `SakshiVaultServiceBinder`). Deletion requests can be for a single file or a batch of files.

When a request arrives, it is processed by the `MediaDeletionScheduler`.

### Initial Validation & Scheduling
The `MediaDeletionScheduler` is responsible for safely transitioning a file into the deletion lifecycle:

1. **Validation**: The `fileId` is verified.
2. **Concurrency Control**: A per-file lock (`MediaFileLocks`) is acquired. This ensures a file isn't concurrently being deleted while an active synchronization or recovery is taking place.
3. **Database Check**: The database (`MediaRecordDao`) is queried. The file must exist and cannot be in an active synchronization state (e.g., `INITIALIZING`, `SYNCING`, `PAUSED`). It must be in a deletable state (e.g., `COMPLETED`, `FAILED`, `STOPPED`).
4. **State Transition**: The file's `state` is updated atomically to `MediaState.DELETE` in the `media_records` table.
5. **Timestamping**: Three distinct time metrics are recorded to track when the file was scheduled (using `DeletionClock`):
    * `deletionScheduledAt`: Wall-clock time (Unix epoch).
    * `deletionScheduledElapsedRealtime`: Monotonic time since boot (`SystemClock.elapsedRealtime()`).
    * `deletionScheduledBootCount`: The device's current boot count.

*Note: This process is idempotent. If a file is already in `MediaState.DELETE`, a repeated deletion request succeeds but does not extend the original scheduling timestamps.*

## 2. Background Task Scheduling

Permanent deletion does not happen immediately. It is delayed by a standard retention period (default 30 days) to allow for recovery.

Once a file is scheduled, the `MediaCleanupScheduler` enqueues background jobs via Jetpack `WorkManager` (`WorkManagerMediaCleanupScheduler`):
* **One-Time Work Request**: A unique, non-overwriting WorkManager job is scheduled to fire exactly when the retention period is calculated to expire for that specific file.
* **Periodic Sweep**: A daily periodic safety-net sweep is ensured. This catches any cleanups that failed or were delayed by clock changes.

Both WorkManager jobs survive process death and device reboots.

### The Ping Fallback Trigger
To guarantee cleanup even if WorkManager is constrained by the OS, the Vault utilizes a `PingCleanupTrigger`. Whenever the Vault receives a keep-alive "ping" from a client, it checks if a background cleanup sweep is running. If not, it asynchronously starts one. Overlapping ping triggers are coalesced into a single running sweep to avoid redundant queries.

## 3. The Cleanup Coordinator

When a cleanup is triggered (either by the WorkManager `MediaCleanupWorker` or the `PingCleanupTrigger`), execution falls to the `MediaCleanupCoordinator`.

The coordinator acquires a process-wide `sweepMutex` to prevent concurrent cleanup sweeps from stepping on each other. It queries the database for all records currently in `MediaState.DELETE` and iterates through them.

## 4. Eligibility and Time Manipulation

For each file in the `DELETE` state, the `DeletionEligibilityChecker` verifies if the 30-day retention period has elapsed. This is where the multiple time metrics recorded earlier are critical to handle device reboots and clock tampering.

### Scenario A: The Device Has Not Rebooted
If the current device `bootCount` matches the `deletionScheduledBootCount` recorded when the file was scheduled, the system relies strictly on the monotonic clock (`elapsedRealtimeMillis`).
* **User changes time backward/forward**: Because the monotonic clock tracks time since the device booted (including deep sleep) independently of the OS wall clock, the deletion timer is entirely immune to the user tampering with their time settings.

### Scenario B: The Device Has Rebooted
If the `bootCount` differs (meaning the device rebooted) or is unavailable, the monotonic clock resets. In this case, the system must fall back to the wall clock, comparing the current `wallTimeMillis` against the original `deletionScheduledAt`.
* **User changes time backward**: The retention period is extended, delaying permanent deletion.
* **User changes time forward**: On an offline device, this can trick the system into believing the retention period has passed, potentially shortening the effective retention time. This is a known limitation that cannot be resolved without a trusted external time source (NTP).

## 5. Permanent File Deletion

If the `DeletionEligibilityChecker` confirms a file's retention period is over, the `MediaCleanupCoordinator` proceeds with permanent removal:

1. **File Lock**: The per-file lock (`MediaFileLocks`) is acquired again.
2. **Re-Verification**: The database record is fetched one last time to ensure it hasn't been recovered or altered concurrently.
3. **Physical Deletion**: The `MediaFileCleaner` is invoked. It attempts to delete the physical media file and its thumbnail from the private application storage. Deletion is idempotent; if the files are already missing, it counts as a success.
4. **Database Record Removal**: Only *after* the physical files are successfully deleted (or verified missing) is the database record removed via `MediaRecordDao.deleteScheduledRecord()`. This SQL command includes a safety check ensuring the `scheduledAt` timestamp matches, preventing the deletion of a record that was deleted, recovered, and re-scheduled in a tiny race window.

If physical deletion fails (e.g., due to temporary I/O errors), the database record remains in `MediaState.DELETE`, and the next periodic sweep or ping will retry.

## 6. Recovery Process

While a file is in `MediaState.DELETE` and its retention period has not expired, it can be recovered via the `MediaRecoveryManager`.

When a recovery request is received:
1. The per-file lock is acquired.
2. The database is checked to ensure the file is actually in `MediaState.DELETE`.
3. The database record's state is atomically reverted to `MediaState.COMPLETED`.
4. All deletion scheduling timestamps (`deletionScheduledAt`, `deletionScheduledElapsedRealtime`, `deletionScheduledBootCount`) are cleared (set to `NULL`).

Once recovered, the file is immediately visible to `listMedia` queries again, and pending WorkManager cleanup jobs for that file will quietly abort when they execute and discover the state has changed.
