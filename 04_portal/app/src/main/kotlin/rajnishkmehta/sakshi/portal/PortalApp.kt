/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal

import android.app.Application
import rajnishkmehta.sakshi.portal.debug.DebugLogger

class PortalApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DebugLogger.init(this)
    }
}
