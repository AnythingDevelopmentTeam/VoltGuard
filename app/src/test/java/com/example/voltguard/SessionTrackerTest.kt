package com.example.voltguard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SessionTrackerTest {

    private fun chargingInfo(level: Int, current: Int = 1000000, temp: Float = 32f) =
        BatteryInfo(
            level = level,
            status = "Charging",
            voltage = 4200,
            temperature = temp,
            currentNow = current
        )

    private fun dischargingInfo(level: Int, current: Int = -1000000, temp: Float = 32f) =
        BatteryInfo(
            level = level,
            status = "Discharging",
            voltage = 3900,
            temperature = temp,
            currentNow = current
        )

    @Test
    fun firstEvent_startSession_noCompleted() {
        val tracker = SessionTracker(RuntimeEnvironment.getApplication())
        assertNull(tracker.onBatteryChanged(chargingInfo(50)))
    }

    @Test
    fun chargingThenDischarging_completesFirstSession() {
        val tracker = SessionTracker(RuntimeEnvironment.getApplication())
        tracker.onBatteryChanged(chargingInfo(50))
        tracker.onBatteryChanged(chargingInfo(51))
        val completed = tracker.onBatteryChanged(dischargingInfo(51))
        assertNotNull(completed)
        assertEquals(SessionType.CHARGE, completed?.type)
        assertEquals(51, completed?.endLevel)
        assertTrue(completed?.duration ?: 0 >= 0)
    }

    @Test
    fun activeSession_tracked() {
        val tracker = SessionTracker(RuntimeEnvironment.getApplication())
        tracker.onBatteryChanged(chargingInfo(50))
        tracker.onBatteryChanged(chargingInfo(51))
        val active = tracker.getActiveSession()
        assertNotNull(active)
        assertEquals(SessionType.CHARGE, active?.type)
        assertEquals(51, active?.startLevel)
    }

    @Test
    fun generatedStats_computesHealthWithinRange() {
        val tracker = SessionTracker(RuntimeEnvironment.getApplication())
        tracker.onBatteryChanged(dischargingInfo(90))
        tracker.onBatteryChanged(chargingInfo(90))
        tracker.onBatteryChanged(chargingInfo(100))
        tracker.onBatteryChanged(dischargingInfo(100))

        val stats = tracker.getStats()
        assertTrue(stats.totalSessions >= 1)
        assertTrue(stats.healthPercent in 0f..100f)
        assertTrue(stats.designCapacity > 0)
    }

    @Test
    fun clearAllSessions_resetsState() {
        val tracker = SessionTracker(RuntimeEnvironment.getApplication())
        tracker.onBatteryChanged(chargingInfo(50))
        tracker.onBatteryChanged(chargingInfo(60))
        tracker.onBatteryChanged(dischargingInfo(60))
        tracker.clearAllSessions()
        assertEquals(0, tracker.getStats().totalSessions)
        assertNull(tracker.getActiveSession())
    }

    @Test
    fun csv_containsHeader_andRows() {
        val tracker = SessionTracker(RuntimeEnvironment.getApplication())
        tracker.onBatteryChanged(chargingInfo(50))
        tracker.onBatteryChanged(chargingInfo(70))
        val csv = tracker.generateCsv()
        assertTrue(csv.startsWith("ID,Type,StartLevel,EndLevel"))
        assertTrue(csv.lineSequence().count() >= 2)
    }
}
