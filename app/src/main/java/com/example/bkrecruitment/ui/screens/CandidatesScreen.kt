package com.example.bkrecruitment.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bkrecruitment.data.CandidateDraft
import com.example.bkrecruitment.data.CandidateMatch
import com.example.bkrecruitment.data.CandidateStageFilter
import com.example.bkrecruitment.data.Vacancy
import com.example.bkrecruitment.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun CandidatesScreen(
    candidates: List<CandidateMatch>,
    vacancies: List<Vacancy>,
    onAddCandidate: suspend (CandidateDraft) -> Result<Unit>,
    onAdvanceStage: suspend (Long) -> Result<Unit>,
    onRescheduleInterview: suspend (Long) -> Result<Unit>,
) {
    var query by remember { mutableStateOf("") }
    var stageFilter by remember { mutableStateOf(CandidateStageFilter.All) }
    var expanded by remember { mutableStateOf(false) }
    val expandedCards = remember { mutableStateMapOf<Long, Boolean>() }
    val filteredCandidates = candidates.filter { candidate ->
        val matchesQuery = query.isBlank() ||
            candidate.fullName.contains(query, ignoreCase = true) ||
            candidate.targetRole.contains(query, ignoreCase = true) ||
            candidate.city.contains(query, ignoreCase = true)
        val matchesStage = when (stageFilter) {
            CandidateStageFilter.All -> true
            CandidateStageFilter.Screening -> candidate.stage == "Скрининг"
            CandidateStageFilter.Interview -> candidate.stage == "Интервью"
            CandidateStageFilter.Final -> candidate.stage == "Финал"
        }
        matchesQuery && matchesStage
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle(
                title = "Подходящие кандидаты",
                subtitle = "Список отсортирован по score совпадения с вакансиями Burger King.",
            )
        }
        item {
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Text(if (expanded) "Скрыть форму" else "Добавить кандидата")
            }
        }
        if (expanded) {
            item {
                CandidateForm(
                    vacancies = vacancies,
                    onSave = { draft ->
                        onAddCandidate(draft).onSuccess {
                            expanded = false
                        }
                    },
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                singleLine = true,
                label = { Text("Поиск по имени, роли или городу") },
            )
        }
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                CandidateStageFilter.entries.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = stageFilter == item,
                        onClick = { stageFilter = item },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = CandidateStageFilter.entries.size,
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = AppColors.Highlight,
                            activeContentColor = AppColors.HighlightText,
                        ),
                    ) {
                        Text(item.title)
                    }
                }
            }
        }
        if (filteredCandidates.isEmpty()) {
            item {
                EmptyStateCard(message = "Кандидаты для подбора пока не загружены.")
            }
        } else {
            items(filteredCandidates, key = { "${it.id}-${it.vacancyTitle}" }) { candidate ->
                CandidateRow(
                    candidate = candidate,
                    expanded = expandedCards[candidate.id] == true,
                    onToggleExpanded = {
                        expandedCards[candidate.id] = !(expandedCards[candidate.id] == true)
                    },
                    onAdvanceStage = { onAdvanceStage(candidate.id) },
                    onRescheduleInterview = { onRescheduleInterview(candidate.id) },
                )
            }
        }
    }
}

@Composable
private fun CandidateRow(
    candidate: CandidateMatch,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onAdvanceStage: suspend () -> Result<Unit>,
    onRescheduleInterview: suspend () -> Result<Unit>,
) {
    val scope = rememberCoroutineScope()
    var actionMessage by remember { mutableStateOf<String?>(null) }
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
            MetaRow(candidate.fullName, "${candidate.score} score")
            Text(
                text = "${candidate.targetRole} • ${candidate.city}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.PrimaryText,
            )
            Text(
                text = candidate.phone,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.SecondaryText,
            )
            MetaRow("Доступность: ${candidate.availability}", "Рейтинг ${candidate.rating}")
            MetaRow("Интервью: ${candidate.interviewDate}", candidate.stage)
            Text(
                text = "Рекомендуется на вакансию: ${candidate.vacancyTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.SecondaryText,
            )
            Button(onClick = onToggleExpanded, modifier = Modifier.fillMaxWidth()) {
                Text(if (expanded) "Скрыть действия" else "Действия по кандидату")
            }
            if (expanded) {
                ActionRow(
                    leftLabel = "Следующий этап",
                    rightLabel = "Перенести интервью",
                    onLeftClick = {
                        scope.launch {
                            actionMessage = onAdvanceStage().exceptionOrNull()?.message ?: "Этап кандидата обновлен"
                        }
                    },
                    onRightClick = {
                        scope.launch {
                            actionMessage = onRescheduleInterview().exceptionOrNull()?.message ?: "Интервью перенесено"
                        }
                    },
                )
                Text(
                    text = "Быстрые действия рекрутера: двигать кандидата по воронке и назначать новое время.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.SecondaryText,
                )
                if (actionMessage != null) {
                    Text(
                        text = actionMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Highlight,
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateForm(
    vacancies: List<Vacancy>,
    onSave: suspend (CandidateDraft) -> Result<Unit>,
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("21") }
    var targetRole by remember { mutableStateOf("") }
    var experienceMonths by remember { mutableStateOf("6") }
    var availability by remember { mutableStateOf("Может выйти через 3 дня") }
    var rating by remember { mutableStateOf("4.5") }
    var phone by remember { mutableStateOf("+7 ") }
    var city by remember { mutableStateOf("") }
    var selectedVacancyId by remember { mutableStateOf(vacancies.firstOrNull()?.id) }
    var vacancyMenuExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val selectedVacancy = vacancies.firstOrNull { it.id == selectedVacancyId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Новый кандидат",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.PrimaryText,
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Highlight,
                )
            }
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("ФИО") },
            )
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Возраст") },
            )
            OutlinedTextField(
                value = experienceMonths,
                onValueChange = { experienceMonths = it.filter(Char::isDigit) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Опыт, мес.") },
            )
            OutlinedTextField(
                value = targetRole,
                onValueChange = { targetRole = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Целевая роль") },
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Город") },
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Телефон") },
            )
            OutlinedTextField(
                value = availability,
                onValueChange = { availability = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Готовность выйти") },
            )
            OutlinedTextField(
                value = rating,
                onValueChange = { rating = it.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Рейтинг 0.0 - 5.0") },
            )
            Column {
                Text(
                    text = "Вакансия: ${selectedVacancy?.title ?: "Не выбрана"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.PrimaryText,
                )
                TextButton(onClick = { vacancyMenuExpanded = true }) {
                    Text("Выбрать вакансию")
                }
                DropdownMenu(
                    expanded = vacancyMenuExpanded,
                    onDismissRequest = { vacancyMenuExpanded = false },
                ) {
                    vacancies.forEach { vacancy ->
                        DropdownMenuItem(
                            text = { Text("${vacancy.title} • ${vacancy.city}") },
                            onClick = {
                                selectedVacancyId = vacancy.id
                                if (targetRole.isBlank()) {
                                    targetRole = vacancy.title
                                }
                                if (city.isBlank()) {
                                    city = vacancy.city
                                }
                                vacancyMenuExpanded = false
                            },
                        )
                    }
                }
            }
            Button(
                onClick = {
                    val vacancyId = selectedVacancyId ?: return@Button
                    val ageValue = age.toIntOrNull()
                    val experienceValue = experienceMonths.toIntOrNull()
                    val ratingValue = rating.toDoubleOrNull()
                    if (ageValue == null || experienceValue == null || ratingValue == null) {
                        errorMessage = "Проверьте возраст, опыт и рейтинг."
                        successMessage = null
                        return@Button
                    }
                    isSaving = true
                    errorMessage = null
                    successMessage = null
                    scope.launch {
                        val result = onSave(
                            CandidateDraft(
                                fullName = fullName.trim(),
                                age = ageValue,
                                targetRole = targetRole.trim(),
                                experienceMonths = experienceValue,
                                availability = availability.trim(),
                                rating = ratingValue.coerceIn(0.0, 5.0),
                                phone = phone.trim(),
                                city = city.trim(),
                                vacancyId = vacancyId,
                            ),
                        )
                        result
                            .onSuccess {
                                successMessage = "Кандидат сохранен."
                                fullName = ""
                                age = "21"
                                targetRole = ""
                                experienceMonths = "6"
                                availability = "Может выйти через 3 дня"
                                rating = "4.5"
                                phone = "+7 "
                                city = ""
                            }
                            .onFailure {
                                errorMessage = it.message ?: "Не удалось сохранить кандидата."
                            }
                        isSaving = false
                    }
                },
                enabled = !isSaving &&
                    fullName.isNotBlank() &&
                    targetRole.isNotBlank() &&
                    city.isNotBlank() &&
                    phone.isNotBlank() &&
                    selectedVacancyId != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isSaving) "Сохранение..." else "Сохранить кандидата")
            }
        }
    }
}
