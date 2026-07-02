package com.example.bkrecruitment.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bkrecruitment.data.BranchLoad
import com.example.bkrecruitment.data.DashboardSnapshot
import com.example.bkrecruitment.data.Vacancy
import com.example.bkrecruitment.ui.theme.AppColors

@Composable
fun DashboardScreen(
    snapshot: DashboardSnapshot,
    onOpenVacancies: () -> Unit,
    onOpenCandidates: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle(
                title = "Оперативный обзор найма",
                subtitle = "Открытые позиции и загруженность ресторанов Burger King.",
            )
        }
        item {
            val metrics = listOf(
                Triple("Открытые вакансии", snapshot.stats.openVacancies.toString(), "Фокус"),
                Triple("Кандидаты в воронке", snapshot.stats.candidatePool.toString(), "Пул"),
                Triple("Назначено интервью", snapshot.stats.interviewsScheduled.toString(), "Неделя"),
                Triple("Срочные открытия", snapshot.stats.urgentOpenings.toString(), "Приоритет"),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                metrics.forEach { metric ->
                    MetricCard(
                        label = metric.first,
                        value = metric.second,
                        accent = metric.third,
                        actionLabel = if (metric.first.contains("Кандидаты")) "Открыть" else "Смотреть",
                        onClick = if (metric.first.contains("Кандидаты")) onOpenCandidates else onOpenVacancies,
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(onClick = onOpenVacancies, modifier = Modifier.fillMaxWidth()) {
                    Text("Быстрые вакансии")
                }
                TextButton(onClick = onOpenCandidates, modifier = Modifier.fillMaxWidth()) {
                    Text("Быстрый пул")
                }
            }
        }
        item {
            SectionTitle(
                title = "Срочные позиции",
                subtitle = "Вакансии с высоким приоритетом, которые нужно закрыть первыми.",
            )
        }
        if (snapshot.urgentVacancies.isEmpty()) {
            item {
                EmptyStateCard(message = "Срочных вакансий сейчас нет.")
            }
        } else {
            items(snapshot.urgentVacancies, key = { it.id }) { vacancy ->
                VacancyPriorityCard(
                    vacancy = vacancy,
                    onOpenVacancies = onOpenVacancies,
                    onOpenCandidates = onOpenCandidates,
                )
            }
        }
        item {
            SectionTitle(
                title = "Нагрузка по ресторанам",
                subtitle = "Площадки с наибольшим числом открытых смен.",
            )
        }
        items(snapshot.branches, key = { "${it.city}-${it.address}" }) { branch ->
            BranchCard(
                branch = branch,
                onOpenVacancies = onOpenVacancies,
                onOpenCandidates = onOpenCandidates,
            )
        }
    }
}

@Composable
private fun VacancyPriorityCard(
    vacancy: Vacancy,
    onOpenVacancies: () -> Unit,
    onOpenCandidates: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = vacancy.title,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.PrimaryText,
            )
            MetaRow(vacancy.city, vacancy.salary)
            MetaRow(vacancy.branch, vacancy.shift)
            Text(
                text = "Требуемый опыт: ${vacancy.experienceMonths} мес.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.SecondaryText,
            )
            ActionRow(
                leftLabel = "К вакансиям",
                rightLabel = "К кандидатам",
                onLeftClick = onOpenVacancies,
                onRightClick = onOpenCandidates,
            )
        }
    }
}

@Composable
private fun BranchCard(
    branch: BranchLoad,
    onOpenVacancies: () -> Unit,
    onOpenCandidates: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = branch.city,
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.PrimaryText,
            )
            Text(
                text = branch.address,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.SecondaryText,
            )
            MetaRow("Менеджер: ${branch.manager}", "${branch.openVacancies} вакансии")
            ActionRow(
                leftLabel = "Открыть вакансии",
                rightLabel = "Открыть кандидатов",
                onLeftClick = onOpenVacancies,
                onRightClick = onOpenCandidates,
            )
        }
    }
}
