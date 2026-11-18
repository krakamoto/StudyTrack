# StudyTrack ⏱️📚

**Versione 1.2.0** — app Android offline per monitorare sessioni di studio, con statistiche affidabili e backup portabile.

**StudyTrack** is a Toggl Track-style study time tracker for Android, designed to help students and self-learners track focus sessions, analyze productivity patterns, and stay accountable. Built with 100% Kotlin, Jetpack Compose, Material 3, and Clean Architecture.

---

## ✨ Features

- **⏱️ Live Ticking Timer**
  - Prominent circular stopwatch with a pulsing ring indicator during active sessions.
  - One-tap Toggl-style floating action button (red when running, gray when stopped).
  - Description and project remain editable while the timer is running; changes are saved automatically.
  - Automatic safeguard: starting a new timer automatically concludes any previously active session.

- **✏️ Retroactive Time Adjustments (Edit Start & End Times)**
  - Forgot to start your study timer or left it running by accident? StudyTrack makes it easy to adjust start time, end time, and date retroactively.
  - Accessible via full-screen route or quick inline modal dialog (long-press from today's list).
  - Integrated 24-hour time pickers and calendar date dialogs.

- **📊 Comprehensive Statistics**
  - **Daily Tab**: Zero-filled 7-day chart, today's total, 7-day average, and best study day.
  - **Weekly Tab**: Current week total, streak, and an 8-week trend.
  - **Monthly Tab**: Current month total, streak, and a 12-month trend.
  - **Project Breakdown**: Visual percentage progress bars showing time invested per subject/project over the last 30 days.
  - **Streak & Performance**: Correct streak calculation across the full study history, not just the visible week.

- **📁 Project & Subject Management**
  - Group time entries into subjects/projects (e.g., Mathematics, Machine Learning, History).
  - Predefined 10-color vibrant palette picker.
  - Full CRUD: create, edit, color-code, and delete projects.

- **📜 History & Timeline**
  - Chronological history of all completed study sessions grouped by date.
  - Filter history by specific project.
  - Swipe-to-delete with a Snackbar action to undo the deletion.
  - Tap any past entry to adjust its description, project, date, and timestamps.
  - Input validation prevents sessions that end before they start or begin in the future.

- **🌙 Dark Theme**
  - Dark UI crafted with Material 3: Deep Navy (`#1A1A2E`), Crimson Red (`#E50914`), and Night Purple (`#252545`).
  - Edge-to-edge transparent system bars support.

- **🔒 100% Offline & Private**
  - All data is stored locally in Room SQLite database. No account, telemetry, or cloud connection required.

- **💾 Backup e ripristino**
  - Export a versioned, human-readable JSON backup to any document provider.
  - Import a backup with a confirmation step and full validation before replacing local data.
  - Clear all local data from the Settings screen.

---

## 🛠️ Tech Stack & Architecture

- **Language:** [Kotlin 2.1.0](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (BOM `2024.12.01`) with [Material 3](https://m3.material.io/)
- **Architecture:** MVVM + Clean Architecture (Data, Domain, Presentation layers)
- **Dependency Injection:** [Dagger Hilt 2.54](https://dagger.dev/hilt/) + Hilt Navigation Compose
- **Local Persistence:** [Room 2.6.1](https://developer.android.com/training/data-storage/room) via KSP
- **Concurrency & Reactivity:** Kotlin Coroutines & `StateFlow` / `Flow`
- **Charts:** Native Jetpack Compose animated bar charts (no chart library dependency)
- **Navigation:** Jetpack Navigation Compose with type-safe route parameters
- **Target SDK:** 35 (Android 15) | **Min SDK:** 26 (Android 8.0 Oreo)

---

## 📂 Project Architecture

```
StudyTrack/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/studytrack/
│       │   ├── StudyTrackApplication.kt      # @HiltAndroidApp application entry
│       │   ├── MainActivity.kt               # @AndroidEntryPoint, edge-to-edge
│       │   ├── data/
│       │   │   ├── db/StudyTrackDatabase.kt  # Room database definition (v1)
│       │   │   ├── entities/                 # Room entities (TimeEntryEntity, ProjectEntity)
│       │   │   ├── dao/                      # Room DAOs (TimeEntryDao, ProjectDao)
│       │   │   └── repository/               # StudyTrackRepository & StudyTrackRepositoryImpl
│       │   ├── domain/
│       │   │   ├── models/                   # Domain models (TimeEntry, Project, Statistics)
│       │   │   └── usecases/                 # Business use cases (Start, Stop, Update, Stats)
│       │   ├── di/
│       │   │   ├── DatabaseModule.kt         # Hilt Room provider
│       │   │   └── RepositoryModule.kt       # Hilt Repository binding
│       │   └── ui/
│       │       ├── StudyTrackApp.kt          # Root Scaffold + BottomNavigationBar
│       │       ├── navigation/               # NavGraph & NavDestination definitions
│       │       ├── theme/                    # Color, Type, Theme (Material3 dark theme)
│       │       ├── components/               # TimerDisplay, ProjectChip, TimeEntryCard,
│       │       │                             # ColorPicker, TimePickerDialog
│       │       └── screens/                  # Timer, EditTimeEntry, Statistics, Projects, History
│       │                                     # with corresponding @HiltViewModel classes
│       └── res/
│           ├── values/                       # strings.xml, colors.xml, themes.xml
│           ├── drawable/                     # launcher icons & vector assets
│           └── mipmap-anydpi-v26/            # adaptive app launcher icons
├── gradle/
│   ├── libs.versions.toml                    # Centralized Version Catalog
│   └── wrapper/gradle-wrapper.properties     # Gradle 8.11.1 wrapper
├── settings.gradle.kts
├── build.gradle.kts
└── README.md
```

---

## 🚀 Build Instructions

### Prerequisites
1. **JDK 17** or later.
2. **Android SDK** with Platform API 35 and Build Tools installed.
3. **Android Studio** (Ladybug | 2024.2.1 or newer recommended).

### Command Line
```bash
# Clone or navigate to the directory
cd StudyTrack

# Build debug APK (the Gradle wrapper is included)
./gradlew assembleDebug

# Install on a connected device/emulator
./gradlew installDebug

# Run unit tests and static checks
./gradlew testDebugUnitTest
./gradlew lintDebug
```

The resulting debug APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Screenshots & Mockups

| Timer (Tracking) | History & Swipe Delete | Statistics |
|:---:|:---:|:---:|
| `[ 01:24:32 ]` <br> *Mathematics* <br> 🔴 `[ STOP ]` | 🗓️ *Monday, Nov 18* <br> 🔹 Machine Learning (2h 15m) <br> 🔸 Algorithms (1h 10m) | 📊 **7-Day Trend** <br> Today: 3h 25m <br> Streak: 5 days 🔥 |

| Edit Time Entry | Project Management |
|:---:|:---:|
| 🕒 *Start: 09:30* <br> 🕒 *End: 11:45* <br> 💾 `[ Save Changes ]` | 🟣 *Deep Learning* <br> 🟢 *Software Engineering* <br> ➕ `[ Add Project ]` |

---

## 🔐 Token GitHub per push

Il token non va mai scritto nel codice o in un file tracciato da Git. È disponibile un file locale già ignorato:

```bash
# 1. Apri il file e incolla il PAT
nano .github-token

# 2. Verifica il diff e crea il commit
git status
git add .
git commit -m "feat: improve StudyTrack"

# 3. Push tramite lo script che legge il token dal file locale
./scripts/push-with-token.sh main
```

Il PAT deve avere il permesso minimo **Contents: Read and write** sul repository `krakamoto/StudyTrack`. Il file `.github-token` è presente solo in locale ed è escluso tramite `.gitignore`.

## 🧪 Verifiche rapide

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
```

## 📄 License
This project is open-source and free to use for personal study tracking and educational development.
