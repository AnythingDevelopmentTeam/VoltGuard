package com.example.voltguard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.voltguard.SessionType
import com.example.voltguard.BatterySession

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY startTime ASC")
    suspend fun getAll(): List<SessionEntity>

    @Query("SELECT * FROM sessions ORDER BY startTime ASC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<SessionEntity>

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun count(): Int

    @Query("DELETE FROM sessions")
    suspend fun clear()
}

fun SessionEntity.toBatterySession(): BatterySession =
    BatterySession(
        id = id,
        type = try { SessionType.valueOf(type) } catch (_: Exception) { SessionType.DISCHARGE },
        startLevel = startLevel,
        endLevel = endLevel,
        startTime = startTime,
        endTime = endTime,
        startVoltage = startVoltage,
        endVoltage = endVoltage,
        avgCurrent = avgCurrent,
        avgTemperature = avgTemperature,
        chargeAdded = chargeAdded,
        duration = duration
    )
