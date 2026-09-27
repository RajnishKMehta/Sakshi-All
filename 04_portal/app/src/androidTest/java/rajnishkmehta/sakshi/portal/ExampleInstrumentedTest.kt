package rajnishkmehta.sakshi.portal

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("rajnishkmehta.sakshi.portal.debug", appContext.packageName)
    }
}
