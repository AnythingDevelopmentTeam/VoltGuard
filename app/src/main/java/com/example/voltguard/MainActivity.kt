package com.example.voltguard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.voltguard.ui.theme.VoltGuardTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()
        requestNotificationPermission()
        setContent {
            VoltGuardTheme {
                val context = LocalContext.current
                val settings = SettingsManager.getInstance(context)
                val firstLaunchDone by settings.firstLaunchDone.collectAsState()
                val navigationStyle by settings.navigationStyle.collectAsState()

                if (firstLaunchDone) {
                    when (navigationStyle) {
                        0 -> PagerScreen()
                        1 -> BottomBarScreen()
                    }
                } else {
                    WelcomeScreen(settings = settings)
                }
            }
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val batteryViewModel: BatteryViewModel = viewModel()
    val accuViewModel: AccuViewModel = viewModel()
    val batteryInfo by batteryViewModel.batteryInfo.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Battery") }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Accu") }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = pagerState.currentPage == 3,
                    onClick = { scope.launch { pagerState.animateScrollToPage(3) } },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("About") }
                )
            }
        }
    ) { _ ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
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
                    label = { Text("Battery") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Accu") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("About") }
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
