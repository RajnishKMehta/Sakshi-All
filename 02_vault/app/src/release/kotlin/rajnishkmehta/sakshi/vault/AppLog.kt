/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault

/**
 * Release-specific logging utility for the Vault application.
 *
 * Stub implementation that discards all debug logs and minimally processes error logs,
 * ensuring no sensitive information is leaked in production builds.
 */
object AppLog {

    fun d(tag: String, msg: String) = Unit

    fun e(tag: String, msg: String, tr: Throwable? = null) = Unit
}
