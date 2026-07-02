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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bkrecruitment.data.Vacancy
import com.example.bkrecruitment.data.VacancyFilter
import com.example.bkrecruitment.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun VacanciesScreen(
    vacancies: List<Vacancy>,
    onCyclePriority: suspend (Long) -> Result<Unit>,
    onToggleStatus: suspend (Long) -> Result<Unit>,
) {
    var filter by remember { mutableStateOf(VacancyFilter.All) }
    var query by remember { mutableStateOf("") }
    val expandedCards = remember { mutableStateMapOf<Long, Boolean>() }
    val filteredVacancies = when (filter) {
        VacancyFilter.All -> vacancies
        VacancyFilter.Urgent -> vacancies.filter { it.priority == "Высокий" }
        VacancyFilter.Open -> vacancies.filter { it.status == "Открыта" }
    }.filter {
        query.isBlank() ||
            it.title.contains(query, ignoreCase = true) ||
            it.city.contains(query, ignoreCase = true) ||
            it.branch.contains(query, ignoreCase = true)
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
                title = "Вакансии по ресторанам",
                subtitle = "Фильтруйте приоритетные и открытые позиции по текущей воронке.",
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                singleLine = true,
                label = { Text("Поиск по роли, городу или ресторану") },
            )
        }
        item {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            ) {
                VacancyFilter.entries.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = filter == item,
                        onClick = { filter = item },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = VacancyFilter.entries.size,
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
        if (filteredVacancies.isEmpty()) {
            item {
                EmptyStateCard(message = "По выбранному фильтру вакансии не найдены.")
            }
        } else {
            items(filteredVacancies, key = { it.id }) { vacancy ->
                VacancyRow(
                    vacancy = vacancy,
                    expanded = expandedCards[vacancy.id] == true,
                    onToggleExpanded = {
                        expandedCards[vacancy.id] = !(expandedCards[vacancy.id] == true)
                    },
                    onCyclePriority = { onCyclePriority(vacancy.id) },
                    onToggleStatus = { onToggleStatus(vacancy.id) },
                )
            }
        }
    }
}

@Composable
private fun VacancyRow(
    vacancy: Vacancy,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onCyclePriority: suspend () -> Result<Unit>,
    onToggleStatus: suspend () -> Result<Unit>,
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
            MetaRow(vacancy.title, vacancy.salary)
            Text(
                text = "${vacancy.city}, ${vacancy.branch}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.PrimaryText,
            )
            MetaRow("Смена: ${vacancy.shift}", "Опыт от ${vacancy.experienceMonths} мес.")
            MetaRow("Приоритет: ${vacancy.priority}", vacancy.status)
            Button(onClick = onToggleExpanded, modifier = Modifier.fillMaxWidth()) {
                Text(if (expanded) "Скрыть действия" else "Открыть действия")
            }
            if (expanded) {
                ActionRow(
                    leftLabel = "Сменить приоритет",
                    rightLabel = if (vacancy.status == "Открыта") "Пауза вакансии" else "Открыть вакансию",
                    onLeftClick = {
                        scope.launch {
                            actionMessage = onCyclePriority().exceptionOrNull()?.message ?: "Приоритет обновлен"
                        }
                    },
                    onRightClick = {
                        scope.launch {
                            actionMessage = onToggleStatus().exceptionOrNull()?.message ?: "Статус вакансии обновлен"
                        }
                    },
                )
                Text(
                    text = "Быстрые действия менеджера по найму: поднять срочность и управлять набором по позиции.",
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
