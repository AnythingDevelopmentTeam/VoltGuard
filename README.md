# VoltGuard

Android-приложение для мониторинга состояния батареи, построенное на Kotlin + Jetpack Compose.

## Возможности

### Мониторинг в реальном времени
- Текущий уровень заряда с анимированным круговым индикатором
- Статус зарядки (заряжается / разряжается / полностью заряжен)
- Тип питания (USB / AC / Wireless)
- Температура и напряжение батареи

### Battery Health
- Состояние батареи (Good / Overheat / Dead / Cold / Over Voltage)
- Технология (Li-ion, Li-poly и т.д.)
- Текущая ёмкость (mAh)
- Ток заряда/разряда (mA)
- Количество циклов заряда

### Фоновый сервис (BatteryService)
- Sticky-уведомление в шторке с текущим %
- Push-уведомления при достижении порогов:
  - >= 80% при зарядке
  - <= 20% при разрядке
- Переключатель вкл/выкл сервиса из UI

### UI / Анимации
- Пульсирующий индикатор при зарядке
- Плавные цветовые переходы по уровню заряда
- Staggered-появление карточек (fade + slide-in)
- Glassmorphism-эффект на карточках
- Динамический фон-градиент

## Технический стек

| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Архитектура | MVVM (ViewModel + StateFlow) |
| Сервис | Foreground Service (`specialUse`) |
| API | BatteryManager (API 21+) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

## Структура проекта

```
app/src/main/java/com/example/voltguard/
├── BatteryInfo.kt          # Data class модели батареи
├── BatteryReceiver.kt      # BroadcastReceiver для ACTION_BATTERY_CHANGED
├── BatteryService.kt       # Foreground Service с уведомлениями
├── BatteryViewModel.kt     # ViewModel для управления состоянием
├── BatteryScreen.kt        # Compose UI с анимациями
├── MainActivity.kt         # Точка входа, запрос разрешений
└── ui/theme/
    ├── Color.kt            # Цветовая палитра
    ├── Theme.kt            # Material 3 тема
    └── Type.kt             # Типографика
```

## Разрешения

| Разрешение | Назначение |
|-----------|-----------|
| `FOREGROUND_SERVICE` | Запуск фонового сервиса |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Тип сервиса для мониторинга |
| `POST_NOTIFICATIONS` | Push-уведомления (Android 13+) |

## Сборка

```bash
./gradlew assembleDebug
```

## Лицензия

[GNU General Public License v3.0](LICENSE)
