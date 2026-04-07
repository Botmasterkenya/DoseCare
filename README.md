# 💊 DoseCare

> **Your personal medication companion — never miss a dose again.**

DoseCare is an Android app built with Kotlin and Jetpack Compose that helps users manage medications, track doses, and stay on top of their health routines. Designed offline-first, it syncs seamlessly to the cloud when connected.

---

## 📋 Product Requirements Document (PRD)

### 1. Overview

| Field | Details |
|---|---|
| **App Name** | DoseCare |
| **Platform** | Android (API 26+, target API 36) |
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM + Hilt (Dependency Injection) |
| **Local Storage** | Room Database (SQLite) |
| **Cloud / Auth** | Firebase Auth + Firestore |
| **Notifications** | AlarmManager + NotificationManager |
| **Developer** | Tee ([@Botmasterkenya](https://github.com/Botmasterkenya)) |
| **University** | Meru University of Science and Technology |
| **Course** | Integrated Thinking in Computing |

---

### 2. Problem Statement

Medication non-adherence is a global health crisis. According to the World Health Organization, approximately **50% of patients** with chronic illnesses do not take their medications as prescribed. Missed doses can lead to treatment failure, preventable hospital readmissions, and significantly reduced quality of life.

Existing solutions are too complex, require constant internet, or lack proper reminders and history tracking. DoseCare solves this with a simple offline-first medication reminder and tracking app with smart notifications and adherence analytics.

---

### 3. Target Users

| User Type | Description |
|---|---|
| Chronic Illness Patients | Individuals managing diabetes, hypertension, HIV/AIDS |
| Elderly Users | Older adults who need simple, reliable reminders |
| Caregivers | Family members or nurses managing someone else's meds |
| General Users | Anyone prescribed a multi-medication regimen |

---

### 4. Goals & Success Metrics

| Goal | Metric |
|---|---|
| Users never miss a dose | ≥ 90% dose logging rate after 2 weeks |
| Fast, reliable experience | App loads in < 2 seconds |
| Works without internet | 100% core functionality offline |
| Easy onboarding | User completes setup in < 2 minutes |
| Reliable reminders | Notifications fire within 1 minute of scheduled time |

---

### 5. Features & Implementation Status

#### ✅ Phase 1 — Onboarding & Authentication
- [x] Animated splash screen with brand logo
- [x] 3-page onboarding (shown only on first launch via DataStore)
- [x] Firebase Auth — email/password registration and login
- [x] Persistent login (auto-login on relaunch)
- [x] Forgot password — reset email via Firebase
- [x] Password visibility toggle + real-time form validation

#### ✅ Phase 2 — Medication Management
- [x] Add medications (name, dosage, unit, frequency, reminder time)
- [x] Unit dropdown: mg, ml, tablets, capsules, drops
- [x] Frequency: Daily, Twice daily, Three times daily, Weekly, As needed
- [x] Medication list on Home screen with status badges
- [x] Soft-delete medications (deactivate flag)
- [x] Per-user medication isolation via Firebase userId

#### ✅ Phase 3 — Reminders & Notifications
- [x] AlarmManager setAlarmClock (most reliable — shows in system clock)
- [x] High-priority notification channel (IMPORTANCE_HIGH)
- [x] Notifications fire when app is closed or in background
- [x] Vibration pattern on notification
- [x] Alarm rescheduling after device reboot (BootReceiver)
- [x] MIUI compatibility guidance included
- [x] POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM, VIBRATE permissions

#### ✅ Phase 4 — Dose Tracking
- [x] Mark doses as Taken / Skipped from Home screen
- [x] Today's dose logs per medication
- [x] DoseLog entity with FK to Medication (CASCADE delete)
- [x] Status colors: green = taken, orange = skipped, red = missed

#### ✅ Phase 5 — History & Analytics
- [x] Full dose history screen grouped by date
- [x] Today / Yesterday / formatted date labels
- [x] 30-day history from Room DB
- [x] Consecutive streak calculation (days all doses taken)
- [x] Overall adherence percentage
- [x] Stats row: Streak 🔥 / Adherence % / Total Taken

#### 🔄 Phase 6 — Cloud Sync (Partial)
- [x] Firebase Firestore configured and connected
- [x] Firebase Auth per-user data isolation
- [ ] Full Firestore sync (planned v1.2)
- [ ] Multi-device support (planned v2.0)

---

### 6. Tech Stack

```
DoseCare/
├── UI Layer         →  Jetpack Compose + Material 3 (BOM 2024.12.01)
├── Navigation       →  Navigation Compose 2.8.5
├── State Mgmt       →  ViewModel + StateFlow + collectAsState
├── DI               →  Hilt 2.51.1
├── Local DB         →  Room 2.6.1 (SQLite + KSP)
├── Preferences      →  DataStore Preferences
├── Auth             →  Firebase Authentication (Email/Password)
├── Cloud DB         →  Firebase Firestore (europe-west1)
├── Notifications    →  AlarmManager (setAlarmClock) + NotificationManager
├── Alarms           →  BroadcastReceiver (AlarmReceiver + BootReceiver)
└── Architecture     →  MVVM + Repository Pattern
```

**Key dependency versions:**

| Dependency | Version |
|---|---|
| AGP | 8.7.3 |
| Kotlin | 2.0.21 |
| KSP | 2.0.21-1.0.28 |
| Hilt | 2.51.1 |
| Room | 2.6.1 |
| Firebase BOM | 33.7.0 |
| Compose BOM | 2024.12.01 |
| Navigation Compose | 2.8.5 |

---

### 7. App Screens

| Screen | Status | Description |
|---|---|---|
| Splash | ✅ | Animated logo, checks auth state, navigates accordingly |
| Onboarding | ✅ | 3-page intro, DataStore flag, shown once only |
| Register | ✅ | Firebase Auth account creation with validation |
| Login | ✅ | Firebase sign-in + forgot password dialog |
| Home | ✅ | Greeting, stats row, adherence card, medication cards |
| Add Medication | ✅ | Form with validation + alarm scheduling on save |
| History | ✅ | Grouped dose history + streak + adherence analytics |
| Medication Edit | ⏳ Planned | View and edit existing medication |
| Settings | ⏳ Planned | Notifications, account, preferences |

---

### 8. Data Models

#### `Medication` (Room Entity)

| Field | Type | Description |
|---|---|---|
| id | Int (PK, autoGenerate) | Unique identifier |
| name | String | Medication name |
| dosage | String | Amount per dose |
| unit | String | mg, ml, tablets, etc. |
| frequency | String | Daily, Twice daily, etc. |
| times | String | JSON e.g. `["08:00","20:00"]` |
| startDate | Long | Unix timestamp of start |
| endDate | Long? | Optional end date |
| notes | String | Additional notes |
| color | String | Hex color for card UI |
| isActive | Boolean | Soft delete flag |
| userId | String | Firebase UID |

#### `DoseLog` (Room Entity)

| Field | Type | Description |
|---|---|---|
| id | Int (PK, autoGenerate) | Unique identifier |
| medicationId | Int (FK → Medication) | CASCADE on delete |
| scheduledTime | Long | When the dose was due |
| takenTime | Long? | When actually taken |
| status | String | PENDING/TAKEN/SKIPPED/MISSED |
| notes | String | Optional notes |
| userId | String | Firebase UID |

---

### 9. Architecture

```
User Interface (Jetpack Compose screens)
        │
        ▼
ViewModel (MedicationViewModel / AuthViewModel)
   StateFlow ──► collectAsState() ──► UI recomposition
        │
        ▼
Repository (MedicationRepository)
     ┌───┴────────┐
     ▼            ▼
Room DB        Firebase
(offline)     (cloud sync)
     │
     ▼
AlarmScheduler
     │
     ▼
AlarmReceiver (BroadcastReceiver)
     │
     ▼
NotificationManager ──► User sees reminder
```

---

### 10. Non-Functional Requirements

| Requirement | Status |
|---|---|
| Offline-first — all core features work without internet | ✅ |
| Smooth 60fps UI on mid-range Android devices | ✅ |
| Notifications fire reliably even when app is closed | ✅ |
| Passwords never stored locally (Firebase Auth only) | ✅ |
| Per-user data isolation (userId on all entities) | ✅ |
| Battery-efficient scheduling (setAlarmClock, OS-managed) | ✅ |

---

### 11. Known Limitations (v1.0)

- Android only — no iOS version
- Single reminder time per medication (multiple times planned for v2)
- No drug interaction checker
- No doctor/caregiver sharing portal
- MIUI devices require manual Autostart permission for reliable alarms
- Firestore sync not fully implemented (Room is source of truth)

---

### 12. Future Roadmap

| Version | Feature |
|---|---|
| v1.1 | Medication detail / edit screen |
| v1.2 | Full Firestore offline-first sync |
| v2.0 | Multiple reminder times per medication |
| v2.1 | Adherence charts (weekly/monthly bar graphs) |
| v2.2 | Doctor/caregiver sharing portal |
| v2.3 | Drug interaction checker API |
| v3.0 | AI-powered adherence predictions |
| v3.1 | Play Store public release |

---

### 13. Project Structure

```
com.tee.dosecare/
├── DoseCareApp.kt              ← @HiltAndroidApp + creates notification channel
├── MainActivity.kt             ← @AndroidEntryPoint + Compose setContent
├── data/
│   ├── local/
│   │   ├── Medication.kt       ← Room @Entity
│   │   ├── DoseLog.kt          ← Room @Entity + DoseStatus object
│   │   ├── MedicationDao.kt    ← @Dao CRUD + queries
│   │   ├── DoseLogDao.kt       ← @Dao CRUD + queries
│   │   └── DoseCareDatabase.kt ← @Database singleton
│   └── repository/
│       └── MedicationRepository.kt  ← Unified data access layer
├── di/
│   └── AppModule.kt            ← @Module @InstallIn Hilt providers
├── ui/
│   ├── Navigation.kt           ← Sealed Screen + NavHost
│   ├── auth/
│   │   ├── AuthViewModel.kt    ← Login/Register/Logout/Reset StateFlows
│   │   ├── LoginScreen.kt      ← Login UI + ForgotPasswordDialog
│   │   └── RegisterScreen.kt   ← Registration UI
│   ├── home/
│   │   ├── MedicationViewModel.kt ← Medications + dose logs + streak
│   │   ├── HomeScreen.kt          ← Today's schedule + adherence
│   │   ├── AddMedicationScreen.kt ← Add medication form
│   │   └── HistoryScreen.kt       ← History + streak + analytics
│   ├── onboarding/
│   │   ├── SplashScreen.kt        ← 2.5s animated splash
│   │   └── OnboardingScreen.kt    ← 3-page onboarding
│   └── theme/
│       ├── Color.kt               ← Material 3 color scheme
│       ├── Type.kt                ← Typography
│       └── Theme.kt               ← DoseCareTheme
└── utils/
    ├── Resource.kt             ← Sealed class: Success/Error/Loading
    ├── PreferencesManager.kt  ← DataStore onboarding completion flag
    ├── AlarmScheduler.kt      ← Schedules/cancels AlarmManager alarms
    ├── AlarmReceiver.kt       ← BroadcastReceiver → shows notification
    └── BootReceiver.kt        ← Reschedules all alarms after device reboot
```

---

### 14. Milestones

| Milestone | Status |
|---|---|
| Project setup & all Gradle dependencies | ✅ Complete |
| Material 3 theme (colors, typography) | ✅ Complete |
| Splash + Onboarding flow | ✅ Complete |
| Firebase Auth (Login / Register / Forgot Password) | ✅ Complete |
| Room Database (entities + DAOs) | ✅ Complete |
| Hilt dependency injection wiring | ✅ Complete |
| Home screen with adherence tracking | ✅ Complete |
| Add Medication form + alarm scheduling | ✅ Complete |
| AlarmManager notifications (MIUI-compatible) | ✅ Complete |
| Dose tracking (Taken / Skipped) | ✅ Complete |
| History screen with streaks & analytics | ✅ Complete |
| BootReceiver (alarms survive reboot) | ✅ Complete |
| Firestore cloud sync | 🔄 Partial |
| Medication edit/detail screen | ⏳ Planned |
| Play Store release | ⏳ Planned |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Meerkat (or later)
- JDK 17+
- Firebase project (Auth + Firestore enabled)
- Physical Android device or emulator (API 26+)

### Setup
```bash
git clone https://github.com/Botmasterkenya/DoseCare.git
```
1. Open project in Android Studio
2. Add your `google-services.json` to `app/`
3. Enable Email/Password auth in Firebase Console
4. Create Firestore database in test mode
5. Sync Gradle → Run on device

### ⚠️ MIUI / Xiaomi Alarm Fix
On Xiaomi devices, grant these manually once after installing:
- **Settings → Apps → DoseCare → Battery Saver** → No Restrictions
- **Settings → Apps → DoseCare → Permissions** → Enable Autostart

Without these, MIUI will kill background alarms.

---

## 📄 License

```
MIT License — free to use, modify, and distribute.
```

---

<p align="center">Built with ❤️ by Tee · Kenya 🇰🇪 · Meru University of Science and Technology · 2026</p>
