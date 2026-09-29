/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.debug

import android.content.Context

/**
 * A no-op logger used in release builds for the Sakshi Portal application.
 */
object DebugLogger {
    @JvmStatic
    fun init(context: Context) {}
    @JvmStatic
    fun d(tag: String = "SakshiPortal", message: String, throwable: Throwable? = null) {}
    @JvmStatic
    fun i(tag: String = "SakshiPortal", message: String, throwable: Throwable? = null) {}
    @JvmStatic
    fun w(tag: String = "SakshiPortal", message: String, throwable: Throwable? = null) {}
    @JvmStatic
    fun e(tag: String = "SakshiPortal", message: String, throwable: Throwable? = null) {}
}
