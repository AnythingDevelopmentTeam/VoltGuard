package com.example.voltguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voltguard.ui.theme.VoltGuardTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        setContent {
            val context = LocalContext.current
            val settings = SettingsManager.getInstance(context)
            val firstLaunchDone by settings.firstLaunchDone.collectAsState()
            val navigationStyle by settings.navigationStyle.collectAsState()
            val darkTheme by settings.darkTheme.collectAsState()

            VoltGuardTheme(darkTheme = darkTheme) {

                if (firstLaunchDone) {
                    when (navigationStyle) {
                        0 -> PagerScreen()
                        1 -> BottomBarScreen()
                    }
                } else {
                    WelcomeScreen(settings = settings)
                }

                AutoUpdateDialog()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }
}

@Composable
private fun PagerScreen() {
    val pageCount = 4
    val pageTitles = listOf(
        stringResource(R.string.nav_battery),
        stringResource(R.string.nav_accu),
        stringResource(R.string.nav_settings),
        stringResource(R.string.nav_about)
    )
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val batteryViewModel: BatteryViewModel = viewModel()
    val accuViewModel: AccuViewModel = viewModel()
    val batteryInfo by batteryViewModel.batteryInfo.collectAsState()
    var showLabels by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(pagerState.currentPage) {
        showLabels = true
        delay(2000)
        showLabels = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> BatteryScreen(viewModel = batteryViewModel)
                1 -> AccuScreen(
                    batteryInfo = batteryInfo,
                    accuViewModel = accuViewModel
                )
                2 -> SettingsScreen()
                3 -> AboutScreen()
            }
        }

        PageIndicator(
            pageCount = pageCount,
            currentPage = pagerState.currentPage,
            pageTitles = pageTitles,
            showLabels = showLabels,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    pageTitles: List<String>,
    showLabels: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = showLabels,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = pageTitles.getOrElse(currentPage) { "" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isActive = index == currentPage
                val size by animateDpAsState(
                    targetValue = if (isActive) 10.dp else 6.dp,
                    label = "dot_size"
                )
                val alpha = if (isActive) 1f else 0.4f

                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
private fun BottomBarScreen() {
    val batteryViewModel: BatteryViewModel = viewModel()
    val accuViewModel: AccuViewModel = viewModel()
    val batteryInfo by batteryViewModel.batteryInfo.collectAsState()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_battery)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_accu)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_about)) }
                )
            }
        }
    ) { _ ->
        when (selectedTab) {
            0 -> BatteryScreen(
                viewModel = batteryViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            )
            1 -> AccuScreen(
                batteryInfo = batteryInfo,
                accuViewModel = accuViewModel,
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            )
            2 -> SettingsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            )
            3 -> AboutScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun AutoUpdateDialog() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var downloading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("voltguard_prefs", Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_update_check", 0)
        if (System.currentTimeMillis() - lastCheck < 86400000) return@LaunchedEffect
        val current = BuildConfig.VERSION_NAME
        val result = UpdateChecker.check(current)
        if (result.isNewer) {
            updateInfo = result
        }
        prefs.edit().putLong("last_update_check", System.currentTimeMillis()).apply()
    }

    if (downloading) {
        AlertDialog(
            onDismissRequest = {},
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

    updateInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { updateInfo = null },
            title = { Text(stringResource(R.string.update_available, info.latestVersion)) },
            text = {
                Text(
                    text = stringResource(R.string.download_update),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        downloading = true
                        val ok = ApkDownloader.downloadAndInstall(context, info.apkUrl)
                        if (ok) updateInfo = null
                        downloading = false
                    }
                }) {
                    Text(stringResource(R.string.download_update))
                }
            },
            dismissButton = {
                TextButton(onClick = { updateInfo = null }) {
                    Text("OK")
                }
            }
        )
    }
}
