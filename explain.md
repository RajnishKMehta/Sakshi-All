# Recording System Workflow: Photo and Video

This document explains the comprehensive flow for capturing photos and recording audio/video, detailing the IPC (Inter-Process Communication) and Vault synchronization behavior.

## Photo Capture Workflow
1. **Camera Click**: The user clicks the shutter button to take a photo.
2. **File Generation**: The Camera app generates the file and captures it locally.
3. **Payload Construction**: The Camera app creates a `PhotoRequest` with a unique `fileId`, the content `uri`, and the `mimeType`.
4. **Sending to Vault**: The Camera calls `SakshiClient.sendPhoto(photoRequest)`.
5. **IPC Transmission**: The Sakshi SDK transmits the `PhotoRequest` as a bundle to the Vault's bound service.
6. **Vault Processing**:
   - The Vault receives the bundle in `sendPhoto(...)`.
   - The Vault performs a one-time full file copy from the client's original URI into its secure storage.
7. **Acknowledgement**: Once successfully copied, the Vault creates a `CopyDoneAck` detailing the total bytes copied and the original URI, and calls `VaultResponder.sendPhotoAck(...)` to notify the Camera app.
8. **Completion**: The Camera app receives the `CopyDoneAck`, completes the suspension, and can optionally clean up local files (e.g. revoking URI permissions).

## Video/Audio Recording Workflow
1. **Start Recording**: The user starts a video/audio recording. The Camera begins writing to a local temporary file.
2. **Initiate Sync**: The Camera immediately calls `client.startAVSync(AVSyncRequest(...))` with the file's URI and MIME type.
3. **Continuous Sync (Vault)**:
   - Vault creates an initial record and returns an `INITIALIZING` status.
   - The Vault's internal `SyncScheduler` begins periodic incremental copying. It repeatedly reads newly appended bytes from the active recording file and writes them to the Vault copy.
   - During this, it emits continuous `SYNCING` updates via `AVSyncStatus`, which the Camera app observes via a Kotlin Flow.
4. **Pause Recording (Optional)**:
   - If the user pauses the recording, the Camera calls `client.pauseAVSync(fileId)`.
   - The Vault pauses the incremental copy scheduler and updates its status to `PAUSED`.
5. **Resume Recording (Optional)**:
   - When the user resumes, the Camera calls `client.resumeAVSync(fileId)`.
   - The Vault scheduler resumes the incremental copy loop and transitions the state back to `SYNCING`.
6. **Stop Recording**:
   - The user stops the recording. The local file is finalized.
   - The Camera calls `client.stopAVSync(fileId)`.
   - The Vault halts the periodic scheduler. This safely interrupts the copy loop. It then runs one final pass to ensure any remaining uncopied bytes are fully synchronized.
7. **Final Acknowledgement**: Once the final bytes are copied and verified, the Vault emits a `CopyDoneAck`.
8. **Client Observation**: The Camera, which has been observing via `observeCopyDone(fileId)`, receives the acknowledgement, revokes any temporary URI permissions it granted for the Vault, and considers the process entirely finished.

### Special Case: Stopping while Paused
If a recording is paused (via `pauseAVSync`) and the user decides to stop the recording without resuming:
- The Camera calls `stopAVSync`.
- Even though the scheduler is paused, the Vault processes the stop command.
- It wakes up just long enough to perform one final incremental copy pass to catch any lingering bytes written right before the pause occurred.
- Once completed, it successfully finalizes the recording and emits the `CopyDoneAck`.
- This ensures data integrity and that no data is lost even if stopped from a paused state.
