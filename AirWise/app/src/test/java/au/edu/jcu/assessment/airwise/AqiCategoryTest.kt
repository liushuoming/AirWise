package au.edu.jcu.assessment.airwise

import au.edu.jcu.assessment.airwise.model.AqiCategory
import org.junit.Assert.assertEquals
import org.junit.Test

/** Checks the US EPA category boundaries. */
class AqiCategoryTest {

    @Test
    fun boundaries_matchEpaRanges() {
        assertEquals(AqiCategory.GOOD, AqiCategory.from(0))
        assertEquals(AqiCategory.GOOD, AqiCategory.from(50))
        assertEquals(AqiCategory.MODERATE, AqiCategory.from(51))
        assertEquals(AqiCategory.MODERATE, AqiCategory.from(100))
        assertEquals(AqiCategory.SENSITIVE, AqiCategory.from(101))
        assertEquals(AqiCategory.UNHEALTHY, AqiCategory.from(200))
        assertEquals(AqiCategory.VERY_UNHEALTHY, AqiCategory.from(300))
        assertEquals(AqiCategory.HAZARDOUS, AqiCategory.from(301))
    }

    @Test
    fun everyCategory_hasTextLabelAndAdvice() {
        AqiCategory.entries.forEach {
            assert(it.label.isNotBlank())
            assert(it.advice.isNotBlank())
        }
    }
}
