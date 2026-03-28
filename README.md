# 💊 DoseCare

> **Your personal medication companion — never miss a dose again.**

DoseCare is an Android app built with Kotlin and Jetpack Compose that helps users manage their medications, track doses, and stay on top of their health routines. Designed with simplicity and reliability in mind, DoseCare works offline-first and syncs seamlessly with the cloud.

---

## 📋 Product Requirements Document (PRD)

### 1. Overview

| Field | Details |
|---|---|
| **App Name** | DoseCare |
| **Platform** | Android (API 26+) |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM + Hilt |
| **Local Storage** | Room Database |
| **Cloud/Auth** | Firebase Auth + Firestore |
| **Developer** | Tee ([@Botmasterkenya](https://github.com/Botmasterkenya)) |

---

### 2. Problem Statement

Managing multiple medications is challenging — especially for patients with chronic conditions, the elderly, or caregivers managing someone else's treatment. Missed doses can lead to serious health consequences. Existing solutions are either too complex, require constant internet access, or lack a clean modern experience.

DoseCare solves this by offering a **simple, offline-first medication reminder and tracking app** with optional cloud sync.

---

### 3. Target Users

- Patients managing chronic illnesses (e.g., diabetes, hypertension, HIV)
- Elderly users who need simple reminders
- Caregivers tracking medications for a family member
- Anyone prescribed a multi-medication regimen

---

### 4. Goals & Success Metrics

| Goal | Metric |
|---|---|
| Users never miss a dose | ≥ 90% dose logging rate after 2 weeks |
| Fast, reliable experience | App loads in < 2 seconds |
| Works without internet | 100% core functionality offline |
| Easy onboarding | User completes setup in < 2 minutes |

---

### 5. Features

#### Phase 1 — Onboarding & Authentication *(Current)*

- [ ] Animated onboarding flow (3 screens)
- [ ] Email/password registration via Firebase Auth
- [ ] Login screen with form validation
- [ ] Persistent login (auto-login on relaunch)
- [ ] First-launch detection via DataStore

#### Phase 2 — Medication Management

- [ ] Add / edit / delete medications
- [ ] Set dosage, frequency, and start/end dates
- [ ] Support for multiple medication types (pill, liquid, injection)
- [ ] Medication list with status indicators

#### Phase 3 — Reminders & Notifications

- [ ] Local push notifications for dose reminders
- [ ] Customizable reminder times per medication
- [ ] Snooze and dismiss actions from notification

#### Phase 4 — Dose Tracking

- [ ] Mark doses as taken, skipped, or missed
- [ ] Daily dose history log
- [ ] Adherence stats (weekly/monthly view)
- [ ] Streak tracking for motivation

#### Phase 5 — Health Journal

- [ ] Log symptoms or side effects
- [ ] Add notes per dose entry
- [ ] Export health log as PDF

#### Phase 6 — Cloud Sync

- [ ] Firestore sync for medications and dose history
- [ ] Offline-first with sync on reconnect (Room as source of truth)
- [ ] Multi-device support

---

### 6. Tech Stack

```
DoseCare/
├── UI Layer         →  Jetpack Compose + Material 3
├── Navigation       →  Navigation Compose
├── State Management →  ViewModel + StateFlow
├── DI               →  Hilt
├── Local DB         →  Room
├── Preferences      →  DataStore
├── Auth             →  Firebase Authentication
├── Cloud DB         →  Firebase Firestore
├── Notifications    →  AlarmManager + NotificationManager
└── Architecture     →  MVVM + Repository Pattern
```

---

### 7. App Screens

| Screen | Description |
|---|---|
| Splash | App logo with animated entry |
| Onboarding | 3-page intro (shown once on first launch) |
| Register | Create account with email & password |
| Login | Sign in to existing account |
| Home | Today's medication schedule at a glance |
| Add Medication | Form to add a new medication |
| Medication Detail | View/edit a specific medication |
| History | Past dose logs |
| Stats | Adherence charts and streaks |
| Journal | Symptom and side effect notes |
| Settings | Notifications, account, app preferences |

---

### 8. Non-Functional Requirements

- **Offline First:** All core features work without an internet connection
- **Performance:** Smooth 60fps UI, no jank on low-end devices
- **Accessibility:** Support for large text, content descriptions on all icons
- **Security:** Passwords handled entirely by Firebase Auth (never stored locally)
- **Battery:** Reminders use efficient scheduling to avoid battery drain

---

### 9. Out of Scope (v1.0)

- Drug interaction checker
- Doctor/pharmacy integration
- iOS version
- Wearable (smartwatch) support

---

### 10. Project Structure *(planned)*

```
com.tee.dosecare/
├── data/
│   ├── local/          # Room DB, DAOs, Entities
│   ├── remote/         # Firestore repositories
│   └── repository/     # Unified data access layer
├── di/                 # Hilt modules
├── domain/
│   └── model/          # Domain models
├── ui/
│   ├── onboarding/     # Onboarding screens
│   ├── auth/           # Login & Register screens
│   ├── home/           # Home screen
│   ├── medication/     # Add/Edit/Detail screens
│   ├── history/        # Dose history
│   ├── stats/          # Adherence stats
│   └── theme/          # Color, Typography, Theme
└── utils/              # Helpers, extensions
```

---

### 11. Milestones

| Milestone | Status |
|---|---|
| Project setup & dependencies | 🔄 In Progress |
| Onboarding flow | ⏳ Planned |
| Authentication (Login/Register) | ⏳ Planned |
| Medication CRUD | ⏳ Planned |
| Reminders & Notifications | ⏳ Planned |
| Dose tracking | ⏳ Planned |
| Cloud sync | ⏳ Planned |
| Health journal | ⏳ Planned |
| Polish & release | ⏳ Planned |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- A Firebase project (with Auth and Firestore enabled)

### Setup
1. Clone the repo:
   ```bash
   git clone https://github.com/Botmasterkenya/DoseCare.git
   ```
2. Open in Android Studio
3. Add your `google-services.json` to the `app/` directory
4. Sync Gradle and run on a device or emulator

---

## 📄 License

```
MIT License — feel free to use, modify, and distribute.
```

---

<p align="center">Built with ❤️ by Group 7 · Kenya 🇰🇪</p>
