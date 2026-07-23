package com.example.voltguard

data class BatteryInfo(
    val level: Int = 0,
    val status: String = "Unknown",
    val plugType: String = "None",
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val health: String = "Unknown",
    val technology: String = "Unknown",
    val capacity: Int = 0,
    val chargeCounter: Int = 0,
    val currentNow: Int = 0,
    val cycleCount: Int = 0
)
