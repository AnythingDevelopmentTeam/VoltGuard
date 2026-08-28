package com.example.voltguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_history")
data class LevelPointEntity(
    @PrimaryKey val timestamp: Long,
    val level: Int
)

@Entity(tableName = "temp_history")
data class TempPointEntity(
    @PrimaryKey val timestamp: Long,
    val temperature: Float
)
