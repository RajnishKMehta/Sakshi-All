/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */

package rajnishkmehta.sakshi.camera.debug

import android.content.Context
import android.util.Log

/**
 * A no-op implementation of the debug logger for release builds.
 *
 * This object mimics the signature of the debug logger but contains empty functions
 * to ensure that no logging overhead or disk I/O operations happen in a production release.
 * Further stripped by ProGuard during compilation.
 */
object DebugLogger {

    /**
     * No-op initialization.
     *
     * @param context Ignored context.
     */
    @JvmStatic
    fun init(context: Context) {
        // No-op in release
    }

    /**
     * No-op debug log.
     */
    @JvmStatic
    fun d(tag: String = "SakshiCamera", message: String, throwable: Throwable? = null) {
        // No-op in release
    }

    /**
     * No-op info log.
     */
    @JvmStatic
    fun i(tag: String = "SakshiCamera", message: String, throwable: Throwable? = null) {
        // No-op in release
    }

    /**
     * No-op warning log.
     */
    @JvmStatic
    fun w(tag: String = "SakshiCamera", message: String, throwable: Throwable? = null) {
        // No-op in release
    }

    /**
     * No-op error log.
     */
    @JvmStatic
    fun e(tag: String = "SakshiCamera", message: String, throwable: Throwable? = null) {
        // No-op in release
    }
}
