package com.example.voltguard

data class BatterySession(
    val id: Long = System.currentTimeMillis(),
    val type: SessionType = SessionType.DISCHARGE,
    val startLevel: Int = 0,
    val endLevel: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val startVoltage: Int = 0,
    val endVoltage: Int = 0,
    val avgCurrent: Int = 0,
    val avgTemperature: Float = 0f,
    val chargeAdded: Int = 0,
    val duration: Long = 0L
)

enum class SessionType { CHARGE, DISCHARGE }

data class ChargingStats(
    val totalSessions: Int = 0,
    val totalChargeAdded: Int = 0,
    val totalDischarge: Int = 0,
    val avgChargeSpeed: Float = 0f,
    val avgDischargeSpeed: Float = 0f,
    val estimatedWear: Float = 0f,
    val estimatedCapacity: Int = 0,
    val designCapacity: Int = 4000,
    val healthPercent: Float = 100f,
    val chargeCycles: Int = 0,
    val totalScreenOnTime: Long = 0L,
    val totalScreenOffTime: Long = 0L
)

data class DailyUsage(
    val date: String = "",
    val screenOnTime: Long = 0L,
    val screenOffTime: Long = 0L,
    val chargeTime: Long = 0L,
    val dischargePercent: Int = 0,
    val chargePercent: Int = 0,
    val avgTemperature: Float = 0f
)
