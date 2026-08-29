# VoltGuard

Android battery monitoring app built with Kotlin + Jetpack Compose.

## Features

### Real-time Monitoring
- Animated circular battery indicator with pulse effect when charging
- Charge status (charging / discharging / full)
- Power source (USB / AC / Wireless)
- Temperature and voltage

### Battery Health
- Battery health state (Good / Overheat / Dead / Cold / Over Voltage)
- Technology (Li-ion, Li-poly, etc.)
- Estimated capacity (mAh)
- Charge/discharge current (mA)
- Charge cycle count

### AccuBattery Dashboard
- Battery health ring with estimated health %
- Time-to-full / time-to-empty estimates
- Charge and discharge speed (%/hour)
- Capacity bar (estimated vs design)
- Real-time current / voltage / temperature card
- Session history with per-session stats
- Daily usage breakdown (discharged / charged %, screen-on time)

### Session Tracking
- Automatic charge/discharge session recording
- JSON persistence (up to 200 sessions)
- Per-session stats: start/end level, voltage, current, temperature, duration
- Aggregate charging stats from recent sessions

### Background Service (BatteryService)
- Sticky notification with current battery %
- Push alerts at configurable thresholds
- Auto-starts on app launch
- Feeds SessionTracker on every battery change
- Toggle in Settings

### Settings
- Alert notification toggle
- Low threshold slider (5–40%)
- High threshold slider (60–95%)
- Background service toggle
- Accent color picker (green / blue / teal / purple / orange / red)
- Dynamic color toggle (wallpaper colors, Android 12+)

### Widgets
- 2x1 and small battery widgets
- Background color picker (auto / green / blue / dark / white / purple / red / teal)
- Optional charging status + temperature line

### UI / Animations
- 4-page horizontal pager with iOS-style dot indicators
- Glassmorphism cards with staggered entry animations
- Dynamic background gradient based on charge level
- Immersive full-screen mode
- Material 3 with dynamic color (Android 12+)

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Service | Foreground Service (`specialUse`) |
| API | BatteryManager (API 21+) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

## Project Structure

```
app/src/main/java/com/example/voltguard/
├── BatteryInfo.kt          # Battery data model
├── BatteryReceiver.kt      # BroadcastReceiver for ACTION_BATTERY_CHANGED
├── BatteryService.kt       # Foreground Service with notifications + SessionTracker feed
├── BatteryViewModel.kt     # ViewModel for battery state + service control
├── BatteryScreen.kt        # Main battery UI with animations
├── SessionTracker.kt       # Core session tracking engine, JSON persistence
├── BatterySession.kt       # Data models (BatterySession, ChargingStats, DailyUsage)
├── AccuViewModel.kt        # ViewModel for AccuBattery dashboard
├── AccuScreen.kt           # AccuBattery-like dashboard UI
├── SettingsManager.kt      # SharedPreferences wrapper for thresholds
├── SettingsScreen.kt       # Settings UI (alerts, service toggle)
├── AboutScreen.kt          # About page
├── MainActivity.kt         # Entry point, 4-page navigation, permissions
└── ui/theme/
    ├── Color.kt            # Color palette
    ├── Theme.kt            # Material 3 theme
    └── Type.kt             # Typography
```

## Permissions

| Permission | Purpose |
|-----------|---------|
| `FOREGROUND_SERVICE` | Background service |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Service type for monitoring |
| `POST_NOTIFICATIONS` | Push notifications (Android 13+) |

## Build

```bash
./gradlew assembleDebug
```

## License

[GNU General Public License v3.0](LICENSE)
