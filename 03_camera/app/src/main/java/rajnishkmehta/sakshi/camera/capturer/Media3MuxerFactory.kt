/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.camera.capturer

import android.annotation.SuppressLint
import rajnishkmehta.sakshi.camera.debug.DebugLogger as Log

import androidx.camera.video.internal.muxer.Muxer
import androidx.camera.video.internal.muxer.MuxerFactory
import androidx.media3.common.util.UnstableApi
import androidx.annotation.OptIn

/**
 * Factory class for creating instances of [Media3Muxer].
 *
 * Provides an initialization mechanism to supply custom fragmented MP4 muxers
 * for video capturing workflows.
 */
@OptIn(UnstableApi::class)
class Media3MuxerFactory(private val isFragmented: Boolean) : MuxerFactory {
    @SuppressLint("RestrictedApi")
    override fun create(outputFormat: Int): Muxer {
        Log.d("Media3MuxerFactory", "create Muxer, outputFormat: $outputFormat")
        return Media3Muxer(isFragmented)
    }
}
