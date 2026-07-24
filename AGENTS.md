# VoltGuard — Project Agents Config

## Build & Run
- Build: `gradlew.bat assembleDebug` (with JAVA_HOME set to Android Studio JBR)
- Release: pushes to `v*` tags trigger `.github/workflows/build-release.yml`
- Set repo secrets: `KEYSTORE_PASS`, `KEY_ALIAS`, `KEY_PASS`, `KEYSTORE_B64` (base64 of keystore.jks)
- JAVA_HOME: `C:\Users\levzh\AppData\Local\Programs\Android Studio\jbr`
- Emulator required for runtime testing

## Code Style
- Kotlin, idiomatic Compose
- No comments unless asked
- Material 3, Material Design 3 only
- Recommendations use `Icons.Outlined.*` from `material-icons-extended`
- MVVM: ViewModel + StateFlow

## Versioning (SemVer)
- `MAJOR.MINOR.PATCH-qualifier` (e.g. 1.1.0-alpha.1)
- MAJOR: breaking changes
- MINOR: new features
- PATCH: bugfixes
- qualifier: alpha → beta → rc → release

## Git Rules
- NEVER push without explicit user permission
- Commit locally first, wait for "пушить" / "push"
- Commit messages: conventional commits (`feat:`, `fix:`, `chore:`, `docs:`)
- Language: English for commits, Russian for user communication

## Project Structure
```
app/src/main/java/com/example/voltguard/
├── BatteryInfo.kt              # Data model
├── BatteryReceiver.kt          # BroadcastReceiver
├── BatteryService.kt           # Foreground Service
├── BatteryViewModel.kt         # ViewModel
├── BatteryScreen.kt            # Main UI
├── BatteryRecommendations.kt   # Real-time battery analyzer + tips
├── SettingsManager.kt          # SharedPreferences wrapper
├── SettingsScreen.kt           # Settings UI
├── AboutScreen.kt              # About UI
├── AccuScreen.kt               # AccuBattery-like session stats
├── AccuViewModel.kt            # ViewModel for AccuScreen
├── SessionTracker.kt           # Charge/discharge session tracking
├── MainActivity.kt             # Entry point + navigation
├── LocaleHelper.kt             # Language switching
├── UpdateChecker.kt            # GitHub API update check
├── ApkDownloader.kt            # APK download + install
├── BatteryWidgetProvider.kt    # Home screen widget
└── ui/theme/                   # Theme files
```

## Known Issues
- `material-icons-extended` now in dependencies — use `Icons.Outlined.*` for recommendations
- `EXTRA_CAPACITY`, `EXTRA_CHARGE_COUNTER`, `EXTRA_CURRENT_NOW` not in SDK — use string literals
- `foregroundServiceType="health"` requires medical permissions — use `"specialUse"` instead
