# Deletion, Recovery & Data Retention

Sākṣī Vault implements a specific lifecycle for media deletion to protect against accidental loss while ensuring data is eventually removed when requested.

## Scheduled Deletion (Soft Delete)

When a user deletes a file through Sākṣī Portal, the file is not immediately erased from the device. Instead, Sākṣī Vault places the file into a **Scheduled Deletion** state.

*   **Visibility:** Files scheduled for deletion are immediately hidden from the main media list in Portal. They are moved to a dedicated "Deleted Items" view.
*   **Retention Period:** The Vault retains the file for a specific grace period (e.g., 30 days) before it is permanently destroyed.
*   **Database State:** The Vault database updates the record's state from `COMPLETED` to `DELETE` and records the scheduling timestamps (`deletionScheduledAt`, `deletionScheduledElapsedRealtime`, `deletionScheduledBootCount`).

## Recovery

During the grace period, users can recover media that was scheduled for deletion.

*   When recovered, the file is restored to the main media list.
*   The Vault database transitions the record state back to `COMPLETED` and clears all scheduled deletion timestamps.
*   The original media file and thumbnail remain intact throughout this process.

## Permanent Deletion

Once the grace period expires, or if the user explicitly chooses to permanently delete a file from the "Deleted Items" view, Sākṣī Vault performs a permanent deletion.

1.  **File Deletion:** Vault deletes the raw media file from its private storage using standard OS file deletion commands (`file.delete()`).
2.  **Thumbnail Cleanup:** Vault deletes any associated thumbnails.
3.  **Database Cleanup:** Vault removes the metadata record from its Room3 database.

### Important: No Secure Erasure

As noted in the [Threat Model](threat-model.md), Sākṣī **does not perform secure erasure**. It does not overwrite the deleted file's bytes with zeros or random data.

Because Android runs on modern flash storage (which uses wear leveling and TRIM), secure erasure at the application level is generally ineffective and unreliable. When Vault deletes a file, the OS marks the space as available, but the physical data may remain recoverable by advanced forensic tools until the OS eventually overwrites those specific flash memory blocks with new data.

## Time Tampering Mitigation

Vault uses a background WorkManager task to periodically check for files whose grace period has expired.

To mitigate basic device time tampering (e.g., a user manually changing the system clock to instantly bypass the retention period), Vault's `DeletionEligibilityChecker` uses the device's monotonic clock (`elapsedRealtimeMillis`).
*   If the device has not rebooted (`bootCount` matches), Vault relies on the monotonic clock, which cannot be easily manipulated by the user.
*   It only falls back to the system wall clock (`wallTimeMillis`) after a device reboot.
