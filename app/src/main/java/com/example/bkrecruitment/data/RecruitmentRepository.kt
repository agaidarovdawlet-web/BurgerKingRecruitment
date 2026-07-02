package com.example.bkrecruitment.data

import android.content.ContentValues
import android.content.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class RecruitmentRepository(context: Context) {

    private val dbHelper = BurgerKingDatabaseHelper(context)

    fun loadSnapshot(): RecruitmentSnapshot {
        val db = dbHelper.readableDatabase
        try {
            val stats = db.rawQuery(
                """
                SELECT
                    (SELECT COUNT(*) FROM vacancies WHERE status = 'Открыта') AS open_vacancies,
                    (SELECT COUNT(*) FROM candidates WHERE status IN ('Новый', 'Скрининг', 'Интервью')) AS candidate_pool,
                    (SELECT COUNT(*) FROM applications WHERE stage IN ('Интервью', 'Финал')) AS interviews_scheduled,
                    (SELECT COUNT(*) FROM vacancies WHERE priority = 'Высокий' AND status = 'Открыта') AS urgent_openings
                """.trimIndent(),
                null,
            ).use { cursor ->
                require(cursor.moveToFirst()) { "Не удалось загрузить агрегированные показатели найма." }
                DashboardStats(
                    openVacancies = cursor.getInt(0),
                    candidatePool = cursor.getInt(1),
                    interviewsScheduled = cursor.getInt(2),
                    urgentOpenings = cursor.getInt(3),
                )
            }

            val branches = db.rawQuery(
                """
                SELECT r.city, r.address, r.manager, COUNT(v.id) AS openings
                FROM restaurants r
                LEFT JOIN vacancies v
                    ON v.restaurant_id = r.id
                    AND v.status = 'Открыта'
                GROUP BY r.id, r.city, r.address, r.manager
                ORDER BY openings DESC, r.city
                """.trimIndent(),
                null,
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            BranchLoad(
                                city = cursor.getString(0),
                                address = cursor.getString(1),
                                manager = cursor.getString(2),
                                openVacancies = cursor.getInt(3),
                            ),
                        )
                    }
                }
            }

            val vacancies = db.rawQuery(
                """
                SELECT v.id, v.title, r.city, r.address, v.shift_name, v.salary, v.priority, v.status, v.experience_months
                FROM vacancies v
                JOIN restaurants r ON r.id = v.restaurant_id
                ORDER BY
                    CASE v.priority WHEN 'Высокий' THEN 0 WHEN 'Средний' THEN 1 ELSE 2 END,
                    v.title,
                    r.city
                """.trimIndent(),
                null,
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            Vacancy(
                                id = cursor.getLong(0),
                                title = cursor.getString(1),
                                city = cursor.getString(2),
                                branch = cursor.getString(3),
                                shift = cursor.getString(4),
                                salary = cursor.getString(5),
                                priority = cursor.getString(6),
                                status = cursor.getString(7),
                                experienceMonths = cursor.getInt(8),
                            ),
                        )
                    }
                }
            }

            val candidates = db.rawQuery(
                """
                SELECT
                    c.id,
                    c.full_name,
                    c.target_role,
                    c.city,
                    c.phone,
                    c.availability,
                    c.rating,
                    a.score,
                    a.interview_date,
                    v.title,
                    a.stage
                FROM applications a
                JOIN candidates c ON c.id = a.candidate_id
                JOIN vacancies v ON v.id = a.vacancy_id
                ORDER BY a.score DESC, c.rating DESC, c.full_name
                """.trimIndent(),
                null,
            ).use { cursor ->
                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            CandidateMatch(
                                id = cursor.getLong(0),
                                fullName = cursor.getString(1),
                                targetRole = cursor.getString(2),
                                city = cursor.getString(3),
                                phone = cursor.getString(4),
                                availability = cursor.getString(5),
                                rating = cursor.getDouble(6),
                                score = cursor.getInt(7),
                                interviewDate = cursor.getString(8),
                                vacancyTitle = cursor.getString(9),
                                stage = cursor.getString(10),
                            ),
                        )
                    }
                }
            }

            return RecruitmentSnapshot(
                dashboard = DashboardSnapshot(
                    stats = stats,
                    branches = branches,
                    urgentVacancies = vacancies.filter { it.priority == "Высокий" && it.status == "Открыта" },
                ),
                vacancies = vacancies,
                candidates = candidates,
            )
        } finally {
            db.close()
        }
    }

    fun addCandidate(draft: CandidateDraft) {
        validateCandidateDraft(draft)
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val candidateId = db.insertOrThrow(
                "candidates",
                null,
                ContentValues().apply {
                    put("full_name", draft.fullName)
                    put("age", draft.age)
                    put("target_role", draft.targetRole)
                    put("experience_months", draft.experienceMonths)
                    put("availability", draft.availability)
                    put("rating", draft.rating)
                    put("phone", draft.phone)
                    put("city", draft.city)
                    put("status", "Новый")
                },
            )

            val vacancyInfo = db.rawQuery(
                """
                SELECT v.title, v.experience_months, r.city
                FROM vacancies v
                JOIN restaurants r ON r.id = v.restaurant_id
                WHERE v.id = ? AND v.status = 'Открыта'
                """.trimIndent(),
                arrayOf(draft.vacancyId.toString()),
            ).use { cursor ->
                require(cursor.moveToFirst()) { "Выбранная вакансия не найдена или уже закрыта." }
                Triple(
                    cursor.getString(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                )
            }

            val score = calculateScore(
                draft = draft,
                vacancyExperience = vacancyInfo.second,
                vacancyCity = vacancyInfo.third,
            )

            db.insertOrThrow(
                "applications",
                null,
                ContentValues().apply {
                    put("vacancy_id", draft.vacancyId)
                    put("candidate_id", candidateId)
                    put("score", score)
                    put("interview_date", nextInterviewSlot())
                    put("stage", "Скрининг")
                },
            )

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun cycleVacancyPriority(vacancyId: Long) {
        val db = dbHelper.writableDatabase
        try {
            val currentPriority = db.rawQuery(
                "SELECT priority FROM vacancies WHERE id = ?",
                arrayOf(vacancyId.toString()),
            ).use { cursor ->
                require(cursor.moveToFirst()) { "Вакансия не найдена." }
                cursor.getString(0)
            }
            val nextPriority = when (currentPriority) {
                "Низкий" -> "Средний"
                "Средний" -> "Высокий"
                else -> "Низкий"
            }
            db.update(
                "vacancies",
                ContentValues().apply { put("priority", nextPriority) },
                "id = ?",
                arrayOf(vacancyId.toString()),
            )
        } finally {
            db.close()
        }
    }

    fun toggleVacancyStatus(vacancyId: Long) {
        val db = dbHelper.writableDatabase
        try {
            val currentStatus = db.rawQuery(
                "SELECT status FROM vacancies WHERE id = ?",
                arrayOf(vacancyId.toString()),
            ).use { cursor ->
                require(cursor.moveToFirst()) { "Вакансия не найдена." }
                cursor.getString(0)
            }
            val nextStatus = if (currentStatus == "Открыта") "Приостановлена" else "Открыта"
            db.update(
                "vacancies",
                ContentValues().apply { put("status", nextStatus) },
                "id = ?",
                arrayOf(vacancyId.toString()),
            )
        } finally {
            db.close()
        }
    }

    fun advanceCandidateStage(candidateId: Long) {
        val db = dbHelper.writableDatabase
        try {
            val currentStage = db.rawQuery(
                "SELECT stage FROM applications WHERE candidate_id = ? ORDER BY id DESC LIMIT 1",
                arrayOf(candidateId.toString()),
            ).use { cursor ->
                require(cursor.moveToFirst()) { "Отклик кандидата не найден." }
                cursor.getString(0)
            }
            val nextStage = when (currentStage) {
                "Скрининг" -> "Интервью"
                "Интервью" -> "Финал"
                "Финал" -> "Оформление"
                else -> "Скрининг"
            }
            db.update(
                "applications",
                ContentValues().apply { put("stage", nextStage) },
                "candidate_id = ?",
                arrayOf(candidateId.toString()),
            )
            db.update(
                "candidates",
                ContentValues().apply { put("status", nextStage) },
                "id = ?",
                arrayOf(candidateId.toString()),
            )
        } finally {
            db.close()
        }
    }

    fun rescheduleCandidateInterview(candidateId: Long) {
        val db = dbHelper.writableDatabase
        try {
            val nextDate = LocalDateTime.of(LocalDate.now().plusDays(4), java.time.LocalTime.of(15, 30))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            db.update(
                "applications",
                ContentValues().apply {
                    put("interview_date", nextDate)
                    put("stage", "Интервью")
                },
                "candidate_id = ?",
                arrayOf(candidateId.toString()),
            )
            db.update(
                "candidates",
                ContentValues().apply { put("status", "Интервью") },
                "id = ?",
                arrayOf(candidateId.toString()),
            )
        } finally {
            db.close()
        }
    }

    private fun calculateScore(
        draft: CandidateDraft,
        vacancyExperience: Int,
        vacancyCity: String,
    ): Int {
        val experienceBonus = (draft.experienceMonths - vacancyExperience).coerceAtLeast(0)
        val cityBonus = if (draft.city.equals(vacancyCity, ignoreCase = true)) 8 else 0
        val ratingBonus = (draft.rating * 6).roundToInt()
        return (62 + draft.experienceMonths + experienceBonus + cityBonus + ratingBonus)
            .coerceIn(65, 99)
    }

    private fun nextInterviewSlot(): String {
        val date = LocalDate.now().plusDays(2)
        return LocalDateTime.of(date, java.time.LocalTime.of(11, 0))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }

    private fun validateCandidateDraft(draft: CandidateDraft) {
        require(draft.fullName.isNotBlank()) { "Укажите ФИО кандидата." }
        require(draft.targetRole.isNotBlank()) { "Укажите целевую роль." }
        require(draft.city.isNotBlank()) { "Укажите город кандидата." }
        require(draft.phone.isNotBlank()) { "Укажите телефон кандидата." }
        require(draft.age in 16..70) { "Возраст должен быть в диапазоне от 16 до 70 лет." }
        require(draft.experienceMonths in 0..600) { "Опыт должен быть в диапазоне от 0 до 600 месяцев." }
        require(draft.rating in 0.0..5.0) { "Рейтинг должен быть от 0.0 до 5.0." }
    }
}
