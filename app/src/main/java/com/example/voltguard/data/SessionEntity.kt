package com.example.voltguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.voltguard.SessionType

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: Long,
    val type: String,
    val startLevel: Int,
    val endLevel: Int,
    val startTime: Long,
    val endTime: Long,
    val startVoltage: Int,
    val endVoltage: Int,
    val avgCurrent: Int,
    val avgTemperature: Float,
    val chargeAdded: Int,
    val duration: Long
) {
    companion object {
        fun from(type: SessionType, data: SessionColumns): SessionEntity =
            SessionEntity(
                id = data.id,
                type = type.name,
                startLevel = data.startLevel,
                endLevel = data.endLevel,
                startTime = data.startTime,
                endTime = data.endTime,
                startVoltage = data.startVoltage,
                endVoltage = data.endVoltage,
                avgCurrent = data.avgCurrent,
                avgTemperature = data.avgTemperature,
                chargeAdded = data.chargeAdded,
                duration = data.duration
            )
    }
}

data class SessionColumns(
    val id: Long,
    val startLevel: Int,
    val endLevel: Int,
    val startTime: Long,
    val endTime: Long,
    val startVoltage: Int,
    val endVoltage: Int,
    val avgCurrent: Int,
    val avgTemperature: Float,
    val chargeAdded: Int,
    val duration: Long
)
