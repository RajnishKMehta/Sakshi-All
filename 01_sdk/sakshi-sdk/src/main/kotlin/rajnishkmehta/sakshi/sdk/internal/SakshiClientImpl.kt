package rajnishkmehta.sakshi.sdk.internal

import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import rajnishkmehta.sakshi.sdk.api.SakshiClient
import rajnishkmehta.sakshi.sdk.api.SakshiClientConfig
import rajnishkmehta.sakshi.sdk.api.SakshiError
import rajnishkmehta.sakshi.sdk.api.SakshiResult
import rajnishkmehta.sakshi.sdk.api.models.CopyDoneAck
import rajnishkmehta.sakshi.sdk.api.models.PhotoRequest
import rajnishkmehta.sakshi.sdk.api.models.RecordingQueryResponse
import rajnishkmehta.sakshi.sdk.api.models.VaultPingResponse
import rajnishkmehta.sakshi.sdk.api.models.AVSyncRequest
import rajnishkmehta.sakshi.sdk.api.models.AVSyncStatus
import rajnishkmehta.sakshi.sdk.internal.ipc.AidlMappers
import rajnishkmehta.sakshi.sdk.internal.ipc.ISakshiVaultCallback
import rajnishkmehta.sakshi.sdk.internal.ipc.VaultServiceConnection
import kotlin.coroutines.resume

/**
 * Concrete implementation of [SakshiClient].
 *
 * Interacts with Vault application via [VaultServiceConnection] and AIDL interfaces.
 */
internal class SakshiClientImpl(
    context: Context,
    config: SakshiClientConfig
) : SakshiClient {

    private val serviceConnection: VaultServiceConnection = VaultServiceConnection(context, config)

    override suspend fun pingVault(): SakshiResult<VaultPingResponse> {
        val serviceResult = serviceConnection.getService()
        if (serviceResult.isFailure) {
            return SakshiResult.Failure(serviceResult.errorOrNull()!!)
        }

        val service = serviceResult.getOrNull()!!
        val startTime = System.currentTimeMillis()

        return try {
            val reqBundle = Bundle().apply {
                putLong("client_timestamp", startTime)
            }
            val resBundle = service.ping(reqBundle)
            val pingResponse = AidlMappers.toVaultPingResponse(resBundle).copy(
                responseTimeMs = System.currentTimeMillis() - startTime
            )
            SakshiResult.Success(pingResponse)
        } catch (e: Throwable) {
            SakshiResult.Failure(
                SakshiError.IpcError(message = e.message ?: "Ping IPC transaction failed", cause = e)
            )
        }
    }

    override suspend fun sendPhoto(request: PhotoRequest): SakshiResult<CopyDoneAck> {
        val serviceResult = serviceConnection.getService()
        if (serviceResult.isFailure) {
            return SakshiResult.Failure(serviceResult.errorOrNull()!!)
        }

        val service = serviceResult.getOrNull()!!

        return suspendCancellableCoroutine { continuation ->
            val callback = object : ISakshiVaultCallback.Stub() {
                override fun onPhotoAck(responseBundle: Bundle) {
                    val response = AidlMappers.toCopyDoneAck(responseBundle)
                    if (continuation.isActive) {
                        continuation.resume(SakshiResult.Success(response))
                    }
                }

                override fun onAVSyncStatus(syncStatusBundle: Bundle) {}
                override fun onCopyDone(copyDoneBundle: Bundle) {}

                override fun onError(errorBundle: Bundle) {
                    val error = AidlMappers.toSakshiError(errorBundle)
                    if (continuation.isActive) {
                        continuation.resume(SakshiResult.Failure(error))
                    }
                }
            }

            try {
                val photoBundle = AidlMappers.toBundle(request)
                service.sendPhoto(photoBundle, callback)
            } catch (e: Throwable) {
                if (continuation.isActive) {
                    continuation.resume(
                        SakshiResult.Failure(
                            SakshiError.IpcError(message = e.message ?: "Failed to send photo", cause = e)
                        )
                    )
                }
            }
        }
    }

    override fun startAVSync(request: AVSyncRequest): Flow<SakshiResult<AVSyncStatus>> = callbackFlow {
        val serviceResult = serviceConnection.getService()
        if (serviceResult.isFailure) {
            trySend(SakshiResult.Failure(serviceResult.errorOrNull()!!))
            close()
            return@callbackFlow
        }

        val service = serviceResult.getOrNull()!!

        val callback = object : ISakshiVaultCallback.Stub() {
            override fun onPhotoAck(responseBundle: Bundle) {}

            override fun onAVSyncStatus(syncStatusBundle: Bundle) {
                val status = AidlMappers.toAVSyncStatus(syncStatusBundle)
                trySend(SakshiResult.Success(status))
                if (status.isCompleted || status.state == AVSyncStatus.State.FAILED || status.state == AVSyncStatus.State.STOPPED) {
                    close()
                }
            }

            override fun onCopyDone(copyDoneBundle: Bundle) {
                val copyDone = AidlMappers.toCopyDoneAck(copyDoneBundle)
                val status = AVSyncStatus(
                    fileId = copyDone.fileId,
                    state = AVSyncStatus.State.COMPLETED,
                    lastCopiedOffsetBytes = copyDone.totalCopiedBytes,
                    totalBytes = copyDone.totalCopiedBytes,
                    isCompleted = true,
                    message = "Copy completed. Original URI: ${copyDone.originalUri}"
                )
                trySend(SakshiResult.Success(status))
                close()
            }

            override fun onError(errorBundle: Bundle) {
                val error = AidlMappers.toSakshiError(errorBundle)
                trySend(SakshiResult.Failure(error))
                close()
            }
        }

        try {
            val syncBundle = AidlMappers.toBundle(request)
            service.startAVSync(syncBundle, callback)
        } catch (e: Throwable) {
            trySend(
                SakshiResult.Failure(
                    SakshiError.IpcError(message = e.message ?: "Failed to start audio/video sync", cause = e)
                )
            )
            close()
        }

        awaitClose {}
    }

    override fun observeCopyDone(fileId: String): Flow<SakshiResult<CopyDoneAck>> = callbackFlow {
        val serviceResult = serviceConnection.getService()
        if (serviceResult.isFailure) {
            trySend(SakshiResult.Failure(serviceResult.errorOrNull()!!))
            close()
            return@callbackFlow
        }

        val service = serviceResult.getOrNull()!!

        val callback = object : ISakshiVaultCallback.Stub() {
            override fun onPhotoAck(responseBundle: Bundle) {}
            override fun onAVSyncStatus(syncStatusBundle: Bundle) {}

            override fun onCopyDone(copyDoneBundle: Bundle) {
                val copyDone = AidlMappers.toCopyDoneAck(copyDoneBundle)
                if (copyDone.fileId == fileId || fileId.isEmpty()) {
                    trySend(SakshiResult.Success(copyDone))
                    close()
                }
            }

            override fun onError(errorBundle: Bundle) {
                val error = AidlMappers.toSakshiError(errorBundle)
                trySend(SakshiResult.Failure(error))
                close()
            }
        }

        try {
            service.stopAVSync(fileId, callback)
        } catch (e: Throwable) {
            trySend(
                SakshiResult.Failure(
                    SakshiError.IpcError(message = e.message ?: "Failed to observe copy done event", cause = e)
                )
            )
            close()
        }

        awaitClose {}
    }

    override suspend fun stopAVSync(fileId: String): SakshiResult<Unit> {
        val serviceResult = serviceConnection.getService()
        if (serviceResult.isFailure) {
            return SakshiResult.Failure(serviceResult.errorOrNull()!!)
        }

        val service = serviceResult.getOrNull()!!

        return suspendCancellableCoroutine { continuation ->
            val callback = object : ISakshiVaultCallback.Stub() {
                override fun onPhotoAck(responseBundle: Bundle) {}
                override fun onAVSyncStatus(syncStatusBundle: Bundle) {
                    val status = AidlMappers.toAVSyncStatus(syncStatusBundle)
                    if (status.state == AVSyncStatus.State.COMPLETED) {
                        if (continuation.isActive) {
                            continuation.resume(SakshiResult.Success(Unit))
                        }
                    } else if (status.state == AVSyncStatus.State.FAILED || status.state == AVSyncStatus.State.STOPPED) {
                        if (continuation.isActive) {
                            continuation.resume(SakshiResult.Failure(SakshiError.Unknown(status.message ?: "Unexpected terminal state: ${status.state}", null)))
                        }
                    }
                }
                override fun onCopyDone(copyDoneBundle: Bundle) {
                    if (continuation.isActive) {
                        continuation.resume(SakshiResult.Success(Unit))
                    }
                }

                override fun onError(errorBundle: Bundle) {
                    val error = AidlMappers.toSakshiError(errorBundle)
                    if (continuation.isActive) {
                        continuation.resume(SakshiResult.Failure(error))
                    }
                }
            }

            try {
                service.stopAVSync(fileId, callback)
            } catch (e: Throwable) {
                if (continuation.isActive) {
                    continuation.resume(
                        SakshiResult.Failure(
                            SakshiError.IpcError(message = e.message ?: "Failed to stop audio/video sync", cause = e)
                        )
                    )
                }
            }
        }
    }

    override suspend fun pauseAVSync(fileId: String): SakshiResult<Unit> {
        val serviceResult = serviceConnection.getService()
        if (serviceResult.isFailure) {
            return SakshiResult.Failure(serviceResult.errorOrNull()!!)
        }

        val service = serviceResult.getOrNull()!!

        return suspendCancellableCoroutine { continuation ->
            val callback = object : ISakshiVaultCallback.Stub() {
                override fun onPhotoAck(responseBundle: Bundle) {}
                override fun onAVSyncStatus(syncStatusBundle: Bundle) {
                    val status = AidlMappers.toAVSyncStatus(syncStatusBundle)
                    if (status.state == AVSyncStatus.State.PAUSED) {
                        if (continuation.isActive) {
                            continuation.resume(SakshiResult.Success(Unit))
                        }
                    } else if (status.state == AVSyncStatus.State.FAILED || status.state == AVSyncStatus.State.STOPPED || status.state == AVSyncStatus.State.COMPLETED) {
                        if (continuation.isActive) {
                            continuation.resume(SakshiResult.Failure(SakshiError.Unknown(status.message ?: "Unexpected sync state: ${status.state}", null)))
                        }
                    }
                }
                override fun onCopyDone(copyDoneBundle: Bundle) {}

                override fun onError(errorBundle: Bundle) {
                    val error = AidlMappers.toSakshiError(errorBundle)
                    if (continuation.isActive) {
                        continuation.resume(SakshiResult.Failure(error))
                    }
                }
            }

            try {
                service.pauseAVSync(fileId, callback)
            } catch (e: Throwable) {
                if (continuation.isActive) {
                    continuation.resume(
                        SakshiResult.Failure(
                            SakshiError.IpcError(message = e.message ?: "Failed to pause audio/video sync", cause = e)
                        )
                    )
                }
            }
        }
    }

    override suspend fun resumeAVSync(fileId: String): SakshiResult<Unit> {
        val serviceResult = serviceConnection.getService()
        if (serviceResult.isFailure) {
            return SakshiResult.Failure(serviceResult.errorOrNull()!!)
        }

        val service = serviceResult.getOrNull()!!

        return suspendCancellableCoroutine { continuation ->
            val callback = object : ISakshiVaultCallback.Stub() {
                override fun onPhotoAck(responseBundle: Bundle) {}
                override fun onAVSyncStatus(syncStatusBundle: Bundle) {
                    val status = AidlMappers.toAVSyncStatus(syncStatusBundle)
                    if (status.state == AVSyncStatus.State.SYNCING) {
                        if (continuation.isActive) {
                            continuation.resume(SakshiResult.Success(Unit))
                        }
                    } else if (status.state == AVSyncStatus.State.FAILED || status.state == AVSyncStatus.State.STOPPED || status.state == AVSyncStatus.State.COMPLETED) {
                        if (continuation.isActive) {
                            continuation.resume(SakshiResult.Failure(SakshiError.Unknown(status.message ?: "Unexpected sync state: ${status.state}", null)))
                        }
                    }
                }
                override fun onCopyDone(copyDoneBundle: Bundle) {}

                override fun onError(errorBundle: Bundle) {
                    val error = AidlMappers.toSakshiError(errorBundle)
                    if (continuation.isActive) {
                        continuation.resume(SakshiResult.Failure(error))
                    }
                }
            }

            try {
                service.resumeAVSync(fileId, callback)
            } catch (e: Throwable) {
                if (continuation.isActive) {
                    continuation.resume(
                        SakshiResult.Failure(
                            SakshiError.IpcError(message = e.message ?: "Failed to resume audio/video sync", cause = e)
                        )
                    )
                }
            }
        }
    }

    override suspend fun isAVSynced(fileId: String): SakshiResult<RecordingQueryResponse> {
        val serviceResult = serviceConnection.getService()
        if (serviceResult.isFailure) {
            return SakshiResult.Failure(serviceResult.errorOrNull()!!)
        }

        val service = serviceResult.getOrNull()!!

        return try {
            val resBundle = service.isAVSynced(fileId)
            val queryResponse = AidlMappers.toRecordingQueryResponse(fileId, resBundle)
            SakshiResult.Success(queryResponse)
        } catch (e: Throwable) {
            SakshiResult.Failure(
                SakshiError.IpcError(
                    message = e.message ?: "Failed to query recording status",
                    cause = e
                )
            )
        }
    }

    override fun disconnect() {
        serviceConnection.disconnect()
    }
}
