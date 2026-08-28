package com.example.voltguard.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {

    @Insert
    suspend fun insertAllLevels(points: List<LevelPointEntity>)

    @Insert
    suspend fun insertAllTemps(points: List<TempPointEntity>)

    @Query("SELECT * FROM level_history ORDER BY timestamp ASC")
    suspend fun getLevelPoints(): List<LevelPointEntity>

    @Query("SELECT * FROM temp_history ORDER BY timestamp ASC")
    suspend fun getTempPoints(): List<TempPointEntity>

    @Query("SELECT timestamp FROM level_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLevelTimestamp(): Long?

    @Query("SELECT timestamp FROM temp_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastTempTimestamp(): Long?

    @Query("SELECT COUNT(*) FROM level_history")
    suspend fun levelCount(): Int

    @Query("SELECT COUNT(*) FROM temp_history")
    suspend fun tempCount(): Int

    @Query("DELETE FROM level_history WHERE timestamp NOT IN (SELECT timestamp FROM level_history ORDER BY timestamp DESC LIMIT :max)")
    suspend fun trimLevels(max: Int)

    @Query("DELETE FROM temp_history WHERE timestamp NOT IN (SELECT timestamp FROM temp_history ORDER BY timestamp DESC LIMIT :max)")
    suspend fun trimTemps(max: Int)
}
