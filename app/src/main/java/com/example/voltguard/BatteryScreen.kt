package com.example.voltguard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val batteryInfo by viewModel.batteryInfo.collectAsState()

    val isCharging = batteryInfo.status == "Charging"
    val bgColor = when {
        batteryInfo.level >= 80 -> Color(0xFF1B3A20)
        batteryInfo.level >= 30 -> Color(0xFF3A351B)
        else -> Color(0xFF3A1B1B)
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgColor, MaterialTheme.colorScheme.surface)
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        BatteryCircularIndicator(
            level = batteryInfo.level,
            isCharging = isCharging
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedCard(visible = visible, index = 0) {
            InfoCard(
                label = "Status",
                value = batteryInfo.status,
                icon = {
                    Text(
                        when (batteryInfo.status) {
                            "Charging" -> "~"
                            "Full" -> "\u2713"
                            "Discharging" -> "\u2193"
                            else -> "\u2014"
                        },
                        fontSize = 24.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedCard(visible = visible, index = 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    label = "Temperature",
                    value = "${batteryInfo.temperature}°C",
                    icon = { Text("\u2103", fontSize = 24.sp) },
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    label = "Voltage",
                    value = "${batteryInfo.voltage} mV",
                    icon = { Text("V", fontSize = 24.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedCard(visible = visible, index = 2) {
            InfoCard(
                label = "Power Source",
                value = batteryInfo.plugType,
                icon = {
                    Text(
                        when (batteryInfo.plugType) {
                            "AC" -> "AC"
                            "USB" -> "USB"
                            "Wireless" -> "~"
                            else -> "\u2014"
                        },
                        fontSize = if (batteryInfo.plugType == "AC" || batteryInfo.plugType == "USB") 14.sp else 24.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        AnimatedCard(visible = visible, index = 3) {
            Text(
                text = "Battery Health",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        AnimatedCard(visible = visible, index = 4) {
            InfoCard(
                label = "Health",
                value = batteryInfo.health,
                icon = {
                    when (batteryInfo.health) {
                        "Good" -> Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        "Overheat" -> Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        "Dead" -> Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                        else -> Text("?", fontSize = 24.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedCard(visible = visible, index = 5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    label = "Technology",
                    value = batteryInfo.technology,
                    icon = { Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    label = "Capacity",
                    value = if (batteryInfo.capacity > 0) "${batteryInfo.capacity} mAh" else "N/A",
                    icon = { Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedCard(visible = visible, index = 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val currentMa = batteryInfo.currentNow / 1000
                InfoCard(
                    label = "Current",
                    value = if (currentMa != 0) "${currentMa} mA" else "N/A",
                    icon = { Text(if (currentMa < 0) "\u2191" else "\u2193", fontSize = 24.sp) },
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    label = "Cycles",
                    value = if (batteryInfo.cycleCount > 0) "${batteryInfo.cycleCount}" else "N/A",
                    icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun AnimatedCard(
    visible: Boolean,
    index: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 400, delayMillis = index * 80)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(durationMillis = 400, delayMillis = index * 80)
        )
    ) {
        content()
    }
}

@Composable
private fun BatteryCircularIndicator(
    level: Int,
    isCharging: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = level / 100f,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "battery_progress"
    )

    val animatedColor by animateColorAsState(
        targetValue = when {
            level >= 80 -> Color(0xFF4CAF50)
            level >= 30 -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        },
        animationSpec = tween(600),
        label = "color_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCharging) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isCharging) 0.6f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val outerGlowRadius by animateDpAsState(
        targetValue = if (isCharging) 30.dp else 0.dp,
        animationSpec = tween(600),
        label = "glow_radius"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(220.dp)
    ) {
        if (isCharging) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .blur(outerGlowRadius)
                    .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                    .drawBehind {
                        drawCircle(
                            color = animatedColor.copy(alpha = glowAlpha * 0.4f),
                            radius = size.minDimension / 2
                        )
                    }
            )
        }

        Box(
            modifier = Modifier
                .size(200.dp)
                .graphicsLayer {
                    scaleX = if (isCharging) pulseScale else 1f
                    scaleY = if (isCharging) pulseScale else 1f
                }
                .drawBehind {
                    if (isCharging) {
                        drawCircle(
                            color = animatedColor.copy(alpha = glowAlpha),
                            radius = size.minDimension / 2
                        )
                    }
                }
        )

        androidx.compose.material3.CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(200.dp),
            color = animatedColor,
            trackColor = animatedColor.copy(alpha = 0.12f),
            strokeWidth = 14.dp
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$level",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "percent",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
