# Recruitment Workflow Demo App

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-Jetpack%20Compose-3DDC84?logo=android&logoColor=white)
![Build](https://img.shields.io/badge/Build-GitHub%20Actions-informational)
![Release](https://img.shields.io/badge/Release-v1.0.0-blue)
![License](https://img.shields.io/badge/License-MIT-green)

Unofficial educational Android demo inspired by a fast-food hiring workflow.

## Who This Project Is For

- recruiters evaluating business-style Android apps;
- clients who need a local HR or operations workflow prototype;
- junior interviews where local DB, dashboards, and CRUD flows matter.

## Key Features

- dashboard with vacancies, restaurants, and interview summary;
- vacancies list with search and filters;
- candidates list with workflow stage filtering;
- candidate creation flow linked to vacancies;
- local SQLite bootstrap from SQL asset.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- SQLiteOpenHelper
- SQL bootstrap asset

## Architecture

```text
UI -> Repository -> SQLite asset-backed local database
```

## Screenshots

Screenshot folder:

- [docs/screenshots](docs/screenshots/README.md)

Suggested captures:

- dashboard
- vacancies
- candidates
- create candidate flow
- filters

## GIF Or Video Demo

- APK is distributed via GitHub Releases

## Installation And Run

```bash
git clone https://github.com/agaidarovdawlet-web/BurgerKingRecruitment.git
cd BurgerKingRecruitment
./gradlew assembleDebug
```

APK path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project Structure

```text
app/src/main/java/com/example/bkrecruitment/
├── data/
├── ui/
└── MainActivity.kt
```

## What I Implemented Personally

- workflow-oriented Android UI;
- local SQL asset integration;
- vacancy and candidate management screens;
- search and filter logic;
- APK release workflow.

## Status

Portfolio/demo business app. Useful for showing business-domain Android work beyond simple CRUD templates.

## Plans

- add final screenshots;
- add tests for repository and filtering logic;
- extend stage-based analytics and interview scheduling.
