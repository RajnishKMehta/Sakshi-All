/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.camera.vault

import rajnishkmehta.sakshi.camera.CamConfig

/**
 * How a video recording is transferred to Sakshi Vault.
 */
enum class VideoVaultTransferMode {
    /**
     * The recording is copied to Vault while it is being written, using the SDK's
     * incremental audio/video synchronization API. Used for crash-safe fragmented MP4.
     */
    INCREMENTAL_SYNC,

    /**
     * The finished recording is submitted once with the SDK's `copyFile` API after recording stops.
     * Used for normal MP4, whose file is only complete after it is finalized.
     */
    COPY_AFTER_RECORDING;

    companion object {
        /**
         * Returns the transfer mode for the given video container [format]
         * ([CamConfig.SettingValues.Default.FORMAT_FMP4] or [CamConfig.SettingValues.Default.FORMAT_MPEG4]).
         */
        fun forVideoFormat(format: Int): VideoVaultTransferMode =
            if (format == CamConfig.SettingValues.Default.FORMAT_MPEG4) {
                COPY_AFTER_RECORDING
            } else {
                INCREMENTAL_SYNC
            }
    }
}
