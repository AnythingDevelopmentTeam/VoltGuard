package com.example.voltguard

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AboutScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentVersion = stringResource(R.string.version)
    var updateDialog by remember { mutableStateOf<UpdateDialogState?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.app_name),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "v${stringResource(R.string.version)}",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.about_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        AboutCard(
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = stringResource(R.string.about_title),
            description = stringResource(R.string.about_description)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AboutCard(
            icon = { Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = stringResource(R.string.features),
            description = stringResource(R.string.features_desc)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AboutCard(
            icon = { Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = stringResource(R.string.tech_stack),
            description = stringResource(R.string.tech_stack_desc)
        )

        Spacer(modifier = Modifier.height(12.dp))

        AboutCard(
            icon = { Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = stringResource(R.string.license_title),
            description = stringResource(R.string.license_desc)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/AnythingDevelopmentTeam/VoltGuard")
                )
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = stringResource(R.string.view_on_github),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            onClick = {
                scope.launch {
                    updateDialog = UpdateDialogState.Checking
                    val result = UpdateChecker.check(currentVersion)
                    updateDialog = if (result.isNewer) {
                        UpdateDialogState.Available(result)
                    } else {
                        UpdateDialogState.UpToDate
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = stringResource(R.string.check_updates),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    when (val state = updateDialog) {
        is UpdateDialogState.Checking -> {
            AlertDialog(
                onDismissRequest = { updateDialog = null },
                title = { Text(stringResource(R.string.check_updates)) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.checking))
                    }
                },
                confirmButton = {}
            )
        }
        is UpdateDialogState.UpToDate -> {
            AlertDialog(
                onDismissRequest = { updateDialog = null },
                title = { Text(stringResource(R.string.check_updates)) },
                text = { Text(stringResource(R.string.up_to_date)) },
                confirmButton = {
                    TextButton(onClick = { updateDialog = null }) {
                        Text("OK")
                    }
                }
            )
        }
        is UpdateDialogState.Available -> {
            AlertDialog(
                onDismissRequest = { updateDialog = null },
                title = { Text(stringResource(R.string.check_updates)) },
                text = { Text(stringResource(R.string.update_available, state.info.latestVersion)) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            updateDialog = UpdateDialogState.Downloading
                            val ok = ApkDownloader.downloadAndInstall(context, state.info.apkUrl)
                            if (!ok) updateDialog = null
                        }
                    }) {
                        Text(stringResource(R.string.download_update))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { updateDialog = null }) {
                        Text("OK")
                    }
                }
            )
        }
        is UpdateDialogState.Downloading -> {
            AlertDialog(
                onDismissRequest = { updateDialog = null },
                title = { Text(stringResource(R.string.check_updates)) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.downloading))
                    }
                },
                confirmButton = {}
            )
        }
        null -> {}
    }
}

private sealed class UpdateDialogState {
    data object Checking : UpdateDialogState()
    data object UpToDate : UpdateDialogState()
    data object Downloading : UpdateDialogState()
    data class Available(val info: UpdateInfo) : UpdateDialogState()
}

@Composable
private fun AboutCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String
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
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(24.dp)) {
                icon()
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
