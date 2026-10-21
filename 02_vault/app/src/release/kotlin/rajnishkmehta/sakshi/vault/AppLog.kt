/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.vault

object AppLog {

    fun d(tag: String, msg: String) = Unit

    fun w(tag: String, msg: String, tr: Throwable? = null) = Unit

    fun e(tag: String, msg: String, tr: Throwable? = null) = Unit
}
