# 🎓 Metropolitan University – Premium Android Mobile Application

A complete, production-ready Android mobile application built using **Kotlin**, **Jetpack Compose**, and **Room SQLite Local Persistence** (with offline-caching and simulated Firebase syncing) for **Metropolitan University**. 

Featuring a modern **Material 3 Design Layout**, the application adheres to robust role-based multi-user privilege controls designed for **Master Admins**, **Department Moderators**, and **Students**.

---

## 🚀 Key Modules & Embedded Features

1. **Role-Based Authentication Engine (Master Admin / Moderator / Student):**
   - Universal secure login entry with glassmorphism panels, error-handling validation, and instant redirection based on account attributes.
   - Built-in credentials for live demonstrations:
     - **Master Admin:** `admin@metrouni.bd` (Password: `admin123`) — Full control across registry, privileges, and broadcasts.
     - **Moderator:** `moderator@metrouni.bd` (Password: `mod123`) — Limited context-bound writing permissions.
     - **Student:** `student@metrouni.bd` (Password: `student123`) — Read-only viewer permissions.

2. **Real-time Mobile Client Dashboard (Student Panel):**
   - **Academic Notice Board:** Organized categorized views (Academic, Exam, Emergency, General) with search lookups, pull-to-refresh animation, and attached official PDF brochures with downloading capabilities.
   - **Intra-campus Transit Schedules (Bus Tracker):** Route identifiers, active transit timings, departure periods, and offline cached guides showing directions.
   - **Campus Events Calendar:** Sorts upcoming and past events dynamically. Standardizes rich card views, descriptions, and syllabus guide overlays.
   - **Instant Alerts Inbox (FCM Stream):** Dynamic alerts feed featuring on-device simulated heads-up push notifications.

3. **Master Admin Console (Superuser Command Center):**
   - **CSV Bulk Accounts Upload:** Instantly parses standards-compliant CSV logs (with columns `student_id,username,password,email,department,role`) to generate accounts in the local database (simulating concurrent Firebase Auth configuration). Outputs a precise error report showing validation alerts or duplicate warnings.
   - **Dynamic Permissions Switch:** Directly grant or restrict Moderator privileges (Notices, Events, Bus Schedules, Results portals). Revoking lockouts adapts the moderator's view instantly in real time!
   - **Global Content Publisher:** Form interfaces to publish academic notices, create campus events, or upload bus routes with immediate student-side cache updates.

4. **Moderator Panel (Limited Scope Console):**
   - Tailored home dashboard presenting active privileges assigned by administrative staff.
   - Blocked publishing scopes are dynamically locked out from writing, matching dynamic Firestore schema structures.

5. **Grades & Results Portal:**
   - Features dual options for lookup:
     - **WebView Portal Integration:** Opens the university's responsive student portal directly inside the browser canvas.
     - **Local PDF Catalogs Repository:** Fast viewing and downloading of officially published graded booklets sorted by department.

6. **Clickable Contacts Directory (Contact Us):**
   - Provides launcher hooks for official Facebook pages, Facebook parent groups, Telegram streams, registrant hotline phones, academic support emails, and campus address coordinates linked dynamically with Google Maps intents.

---

## 🛠️ Architecture, Coding Style, and Design Philosophy

- **Modern Stack:** Written 100% in pure **Kotlin**, styled with declarative **Jetpack Compose** (Material 3), and managed via **MVVM architecture with Flow & StateFlow** reactive streams.
- **Local Persistence Layer (Room SQLite Core):** Abstracted through repository patterns to act as an offline-first cache database.
- **Aesthetic Craftsmanship:** Incorporates vibrant color pairings (Blue & White University theme), edge-to-edge system bar rendering, responsive adaptive grid constraints, custom vector icons, ripples, and soft glassmorphism shadows.

---

## 🔥 Firebase Integration & Deployment Setup Guide

The app code is preloaded with architecture classes to sync local persistence with **Google Firebase**. Follow this guide to hook up live cloud syncing:

### Step 1: Initialize Firebase Console
1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project and name it **Metropolitan University Mobile**.
3. Add an Android Application to the project. Enter your `applicationId`: `com.aistudio.metropolitanuniversity` (match the exact value inside `app/build.gradle.kts`).
4. Download the generated `google-services.json` config package.

### Step 2: Inject Google Services File
1. Place your downloaded `google-services.json` file inside the target folder:
   `/app/google-services.json`

### Step 3: Configure Cloud Services Roles
1. **Firebase Authentication:**
   - Navigate to **Authentication -> Sign-in Method** and enable individual **Email/Password** providers.
2. **Cloud Firestore Rules:**
   - Create a Firestore Database. Set rules to honor user metadata profiles to implement role-based constraints:
     ```javascript
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /users/{userId} {
           allow read: if request.auth != null;
           allow write: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "ADMIN";
         }
         match /notices/{noticeId} {
           allow read: if request.auth != null;
           allow write: if get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "ADMIN" || 
                        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "MODERATOR" && 
                         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.permissions.notices == true);
         }
       }
     }
     ```
3. **Cloud Storage & Push Messaging:**
   - Enable Cloud Storage to host result sheets, bus guides, or event banners.
   - Configure Firebase Cloud Messaging (FCM) on-device handlers.

---

## 📦 How to Build the Installer APK

Through the **Google AI Studio** viewer platform, building and downloading is simple:

1. Locate the **Settings Menu (Gear Icon)** in the active top-right panel.
2. Select **Generate APK** or **Generate Signed Release Bundle (.aab)**.
3. Once compilation finishes, a notification will appear to download your `.apk` installer file directly. Put this on any Android mobile device or emulator, install it, and begin testing!
