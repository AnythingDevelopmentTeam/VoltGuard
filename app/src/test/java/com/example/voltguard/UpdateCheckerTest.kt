package com.example.voltguard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    private val checker = UpdateChecker

    @Test
    fun sameVersion_returnsZero() {
        assertEquals(0, checker.compareVersionStrings("1.2.3", "1.2.3"))
    }

    @Test
    fun newerMajorVersion_wins() {
        assertTrue(checker.compareVersionStrings("2.0.0", "1.9.9") > 0)
        assertTrue(checker.compareVersionStrings("1.9.9", "2.0.0") < 0)
    }

    @Test
    fun newerMinorVersion_wins() {
        assertTrue(checker.compareVersionStrings("1.3.0", "1.2.9") > 0)
    }

    @Test
    fun newerPatchVersion_wins() {
        assertTrue(checker.compareVersionStrings("1.2.4", "1.2.3") > 0)
    }

    @Test
    fun missingSegment_treatedAsZero() {
        assertTrue(checker.compareVersionStrings("1.2", "1.2.0") == 0)
        assertTrue(checker.compareVersionStrings("1.2.1", "1.2") > 0)
    }

    @Test
    fun release_succeeds_preRelease_sameNumber() {
        assertTrue(checker.compareVersionStrings("1.3.0", "1.3.0-beta.1") > 0)
        assertTrue(checker.compareVersionStrings("1.3.0-beta.1", "1.3.0") < 0)
    }

    @Test
    fun preReleaseStages_ordered_alpha_beta_rc() {
        assertTrue(checker.compareVersionStrings("1.3.0-beta.1", "1.3.0-alpha.1") > 0)
        assertTrue(checker.compareVersionStrings("1.3.0-rc.1", "1.3.0-beta.1") > 0)
    }

    @Test
    fun higherNumericPreRelease_wins() {
        assertTrue(checker.compareVersionStrings("1.3.0-beta.2", "1.3.0-beta.1") > 0)
    }

    @Test
    fun currentBeta_isOlderThanStableFuture() {
        assertTrue(checker.compareVersionStrings("2.0.0", "1.3.0-beta.1") > 0)
    }
}
