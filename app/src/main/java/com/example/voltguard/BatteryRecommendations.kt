package com.example.voltguard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

data class BatteryRecommendation(
    val icon: ImageVector,
    val titleKey: Int,
    val descKey: Int,
    val priority: Int
)

object BatteryAnalyzer {

    fun analyze(info: BatteryInfo): List<BatteryRecommendation> {
        val list = mutableListOf<BatteryRecommendation>()
        val isCharging = info.status == "Charging"
        val isDischarging = info.status == "Discharging"

        if (info.temperature >= 50f) {
            list += BatteryRecommendation(Icons.Outlined.Whatshot, R.string.rec_scorch_t, R.string.rec_scorch_d, 1)
        } else if (info.temperature >= 40f) {
            list += BatteryRecommendation(Icons.Outlined.Whatshot, R.string.rec_hot_t, R.string.rec_hot_d, 2)
        }

        if (info.health == "Dead") {
            list += BatteryRecommendation(Icons.Outlined.BatteryAlert, R.string.rec_dead_t, R.string.rec_dead_d, 1)
        } else if (info.health == "Overheat") {
            list += BatteryRecommendation(Icons.Outlined.Whatshot, R.string.rec_overheat_t, R.string.rec_overheat_d, 1)
        }

        if (isCharging && info.level >= 100) {
            list += BatteryRecommendation(Icons.Outlined.CheckCircle, R.string.rec_full_t, R.string.rec_full_d, 2)
        } else if (isCharging && info.level >= 80) {
            list += BatteryRecommendation(Icons.Outlined.Lightbulb, R.string.rec_opt80_t, R.string.rec_opt80_d, 3)
        }

        if (isDischarging && info.level <= 10) {
            list += BatteryRecommendation(Icons.Outlined.Warning, R.string.rec_critical_t, R.string.rec_critical_d, 1)
        } else if (isDischarging && info.level <= 20) {
            list += BatteryRecommendation(Icons.Outlined.BatteryAlert, R.string.rec_low_t, R.string.rec_low_d, 2)
        }

        if (isDischarging && info.currentNow < -500000) {
            list += BatteryRecommendation(Icons.Outlined.Bolt, R.string.rec_drain_t, R.string.rec_drain_d, 3)
        }

        if (info.cycleCount > 800) {
            list += BatteryRecommendation(Icons.Outlined.Refresh, R.string.rec_cycles_t, R.string.rec_cycles_d, 3)
        }

        return list.sortedBy { it.priority }
    }
}
