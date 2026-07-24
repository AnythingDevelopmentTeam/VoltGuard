package com.example.voltguard

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(viewModel: BatteryViewModel = viewModel(), modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settings = SettingsManager.getInstance(context)
    val serviceRunning by viewModel.serviceRunning.collectAsState()

    val lowThreshold by settings.lowThreshold.collectAsState()
    val highThreshold by settings.highThreshold.collectAsState()
    val alertsEnabled by settings.alertsEnabled.collectAsState()
    val navigationStyle by settings.navigationStyle.collectAsState()
    val language by settings.language.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsCard(title = stringResource(R.string.navigation)) {
            val options = listOf(
                stringResource(R.string.swipe_pager) to 0,
                stringResource(R.string.bottom_bar) to 1
            )
            options.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = navigationStyle == value,
                            onClick = { settings.setNavigationStyle(value) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = navigationStyle == value,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = stringResource(R.string.language)) {
            val langOptions = listOf(
                "English" to "en",
                "Русский" to "ru"
            )
            langOptions.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = language == value,
                            onClick = {
                                settings.setLanguage(value)
                                LocaleHelper.setLanguage(context, value)
                                (context as? ComponentActivity)?.recreate()
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = language == value,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = stringResource(R.string.service_title)) {
            SettingsRow(label = stringResource(R.string.background_service)) {
                Switch(
                    checked = serviceRunning,
                    onCheckedChange = { viewModel.toggleService() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.service_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val notificationsEnabled = serviceRunning && alertsEnabled

        SettingsCard(title = stringResource(R.string.notifications)) {
            val alpha = if (serviceRunning) 1f else 0.5f

            SettingsRow(label = stringResource(R.string.alert_notifications)) {
                Switch(
                    checked = alertsEnabled,
                    onCheckedChange = { settings.setAlertsEnabled(it) },
                    enabled = serviceRunning
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsRow(label = stringResource(R.string.low_alert_setting)) {
                Text(
                    text = "$lowThreshold%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                )
            }

            Slider(
                value = lowThreshold.toFloat(),
                onValueChange = { settings.setLowThreshold(it.toInt()) },
                valueRange = 5f..40f,
                steps = 34,
                enabled = notificationsEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsRow(label = stringResource(R.string.high_alert_setting)) {
                Text(
                    text = "$highThreshold%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                )
            }

            Slider(
                value = highThreshold.toFloat(),
                onValueChange = { settings.setHighThreshold(it.toInt()) },
                valueRange = 60f..95f,
                steps = 34,
                enabled = notificationsEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.notification_summary, lowThreshold, highThreshold),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SettingsCard(title = stringResource(R.string.about_app)) {
            SettingsRow(label = stringResource(R.string.ver)) {
                Text(
                    text = BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsRow(label = stringResource(R.string.min_sdk)) {
                Text(
                    text = stringResource(R.string.min_sdk_val),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SettingsRow(label = stringResource(R.string.target_sdk)) {
                Text(
                    text = "36",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}
