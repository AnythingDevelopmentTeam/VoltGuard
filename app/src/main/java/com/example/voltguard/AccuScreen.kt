package com.example.voltguard

import android.content.Intent

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccuScreen(
    batteryInfo: BatteryInfo,
    accuViewModel: AccuViewModel,
    modifier: Modifier = Modifier
) {
    val stats by accuViewModel.stats.collectAsState()
    val activeSession by accuViewModel.activeSession.collectAsState()
    val timeToFull by accuViewModel.timeToFull.collectAsState()
    val timeToEmpty by accuViewModel.timeToEmpty.collectAsState()
    val history by accuViewModel.history.collectAsState()
    val dailyUsage by accuViewModel.dailyUsage.collectAsState()
    val chartPoints by accuViewModel.chartPoints.collectAsState()
    val tempPoints by accuViewModel.tempPoints.collectAsState()
    val showClearDialog = remember { mutableStateOf(false) }

    LaunchedEffect(batteryInfo) {
        accuViewModel.onBatteryChanged(batteryInfo)
    }

    val isCharging = batteryInfo.status == "Charging" || batteryInfo.status == "Full"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        if (isCharging) Color(0xFF0D2818) else Color(0xFF1A1A2E),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        ChargeHealthRing(
            healthPercent = stats.healthPercent,
            level = batteryInfo.level
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.battery_health),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = if (isCharging) stringResource(R.string.time_to_full) else stringResource(R.string.time_to_empty),
                value = formatDuration(if (isCharging) timeToFull else timeToEmpty),
                color = if (isCharging) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.charge_speed),
                value = "${String.format("%.1f", stats.avgChargeSpeed)} ${stringResource(R.string.unit_percent_h)}",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = stringResource(R.string.discharge_speed),
                value = "${String.format("%.1f", stats.avgDischargeSpeed)} ${stringResource(R.string.unit_percent_h)}",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.cycles),
                value = "${stats.chargeCycles}",
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = stringResource(R.string.charge_added),
                value = "${stats.totalChargeAdded}%",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.discharged),
                value = "${stats.totalDischarge}%",
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.real_time),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        RealtimeCard(
            currentMa = batteryInfo.currentNow / 1000,
            voltage = batteryInfo.voltage,
            temperature = batteryInfo.temperature
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.capacity),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CapacityRow(
                    label = stringResource(R.string.estimated_capacity),
                    value = "${stats.estimatedCapacity} ${stringResource(R.string.unit_mah)}"
                )
                Spacer(modifier = Modifier.height(8.dp))
                CapacityRow(
                    label = stringResource(R.string.design_capacity),
                    value = "${stats.designCapacity} ${stringResource(R.string.unit_mah)}"
                )
                Spacer(modifier = Modifier.height(12.dp))

                val healthColor = when {
                    stats.healthPercent >= 80 -> Color(0xFF4CAF50)
                    stats.healthPercent >= 50 -> Color(0xFFFFC107)
                    else -> Color(0xFFF44336)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(healthColor.copy(alpha = 0.15f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = stats.healthPercent / 100f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(healthColor)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.health_percent, String.format("%.1f", stats.healthPercent)),
                    style = MaterialTheme.typography.labelSmall,
                    color = healthColor
                )
            }
        }

        if (chartPoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.chart_24h),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                BatteryChart(
                    points = chartPoints,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        if (tempPoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.chart_temp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                BatteryTempChart(
                    points = tempPoints,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        if (dailyUsage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.daily_usage),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            dailyUsage.take(5).forEach { usage ->
                DailyUsageCard(usage)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val context = LocalContext.current
        OutlinedButton(
            onClick = {
                val intent = accuViewModel.exportCsv()
                context.startActivity(Intent.createChooser(intent, null))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.export_csv))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { showClearDialog.value = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.clear_sessions))
        }

        if (showClearDialog.value) {
            AlertDialog(
                onDismissRequest = { showClearDialog.value = false },
                title = { Text(stringResource(R.string.clear_sessions)) },
                text = { Text(stringResource(R.string.clear_sessions_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        accuViewModel.clearSessions()
                        showClearDialog.value = false
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog.value = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun ChargeHealthRing(healthPercent: Float, level: Int) {
    val animatedHealth by animateFloatAsState(
        targetValue = healthPercent / 100f,
        animationSpec = tween(800),
        label = "health_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val healthColor = when {
        healthPercent >= 80 -> Color(0xFF4CAF50)
        healthPercent >= 50 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(180.dp)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
    ) {
        CircularProgressIndicator(
            progress = { animatedHealth },
            modifier = Modifier.size(180.dp),
            color = healthColor,
            trackColor = healthColor.copy(alpha = 0.12f),
            strokeWidth = 12.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${String.format("%.0f", healthPercent)}",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.percent_symbol),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.percent_charged, level.toString()),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RealtimeCard(
    currentMa: Int,
    voltage: Int,
    temperature: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RealtimeItem(stringResource(R.string.current), if (currentMa != 0) "${currentMa} ${stringResource(R.string.unit_ma)}" else stringResource(R.string.na))
            RealtimeItem(stringResource(R.string.voltage), "$voltage ${stringResource(R.string.unit_mv)}")
            RealtimeItem(stringResource(R.string.temperature), "${temperature}${stringResource(R.string.unit_celsius)}")
        }
    }
}

@Composable
private fun RealtimeItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CapacityRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DailyUsageCard(usage: DailyUsage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = usage.date,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.used_charged_format, usage.dischargePercent.toString(), usage.chargePercent.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDuration(usage.screenOnTime),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.screen_on),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0) return "--:--"
    val hours = millis / 3600000
    val minutes = (millis % 3600000) / 60000
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
