/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.portal.ui.viewer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaViewerViewModelTest {

    @Test
    fun testExportStateIdleInitially() {
        // Dummy test to just verify compilation of new sealed class states
        val state = ExportUiState.Idle
        assertEquals(ExportUiState.Idle, state)
    }
}
