# BurgerKingRecruitment

Android-приложение для демонстрации процесса подбора персонала Burger King: вакансии, кандидаты, этапы интервью и локальная база данных.

## Возможности

- дашборд по вакансиям, ресторанам и интервью;
- список вакансий с поиском и фильтрами;
- список кандидатов с фильтрацией по этапам;
- добавление кандидата и привязка к вакансии;
- локальная SQLite-база с первичным наполнением из SQL-скрипта.

## Стек

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- SQLiteOpenHelper
- SQL asset bootstrap

## Данные

Основной SQL-скрипт:

- `app/src/main/assets/burger_king_recruitment.sql`

Параметры Android:

- `applicationId`: `com.example.bkrecruitment`
- `minSdk`: 24
- `targetSdk`: 36
- `compileSdk`: 36

## Запуск

```bash
./gradlew assembleDebug
```

## Состояние проекта на 2 июля 2026

- структура проекта и исходники на месте;
- найдено `15` Kotlin-файлов, Android-манифест и SQL-asset;
- проект выглядит как рабочее локальное demo-приложение;
- автоматическую сборку в этой среде подтвердить не удалось: Gradle wrapper не смог скачать `gradle-8.13` из-за `SocketTimeoutException`.

## Ограничения

- итоговый статус сборки нужно перепроверить в Android Studio или в среде с доступом к `services.gradle.org`;
- тесты в репозитории явно не выделены;
- данные ориентированы на локальную демо-базу, а не на production-backend.
