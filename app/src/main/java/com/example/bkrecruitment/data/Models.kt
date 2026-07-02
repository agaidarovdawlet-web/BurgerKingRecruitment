package com.example.bkrecruitment.data

data class DashboardStats(
    val openVacancies: Int,
    val candidatePool: Int,
    val interviewsScheduled: Int,
    val urgentOpenings: Int,
)

data class BranchLoad(
    val city: String,
    val address: String,
    val manager: String,
    val openVacancies: Int,
)

data class Vacancy(
    val id: Long,
    val title: String,
    val city: String,
    val branch: String,
    val shift: String,
    val salary: String,
    val priority: String,
    val status: String,
    val experienceMonths: Int,
)

data class CandidateMatch(
    val id: Long,
    val fullName: String,
    val targetRole: String,
    val city: String,
    val phone: String,
    val availability: String,
    val rating: Double,
    val score: Int,
    val interviewDate: String,
    val vacancyTitle: String,
    val stage: String,
)

data class DashboardSnapshot(
    val stats: DashboardStats,
    val branches: List<BranchLoad>,
    val urgentVacancies: List<Vacancy>,
)

enum class VacancyFilter(val title: String) {
    All("Все"),
    Urgent("Срочно"),
    Open("Открыты"),
}

enum class CandidateStageFilter(val title: String) {
    All("Все"),
    Screening("Скрининг"),
    Interview("Интервью"),
    Final("Финал"),
}

data class CandidateDraft(
    val fullName: String,
    val age: Int,
    val targetRole: String,
    val experienceMonths: Int,
    val availability: String,
    val rating: Double,
    val phone: String,
    val city: String,
    val vacancyId: Long,
)

data class RecruitmentSnapshot(
    val dashboard: DashboardSnapshot,
    val vacancies: List<Vacancy>,
    val candidates: List<CandidateMatch>,
)
