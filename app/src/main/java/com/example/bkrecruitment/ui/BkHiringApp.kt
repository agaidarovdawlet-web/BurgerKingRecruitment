package com.example.bkrecruitment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bkrecruitment.data.CandidateDraft
import com.example.bkrecruitment.data.RecruitmentRepository
import com.example.bkrecruitment.data.RecruitmentSnapshot
import com.example.bkrecruitment.ui.screens.CandidatesScreen
import com.example.bkrecruitment.ui.screens.DashboardScreen
import com.example.bkrecruitment.ui.screens.VacanciesScreen
import com.example.bkrecruitment.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Dashboard("dashboard", "Обзор", Icons.Outlined.SpaceDashboard),
    Vacancies("vacancies", "Вакансии", Icons.Outlined.HomeWork),
    Candidates("candidates", "Кандидаты", Icons.Outlined.Groups),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BkHiringApp() {
    val context = LocalContext.current
    val repository = remember(context) { RecruitmentRepository(context) }
    val navController = rememberNavController()
    var reloadKey by remember { mutableIntStateOf(0) }
    val snapshotState by produceState<RecruitmentSnapshot?>(initialValue = null, repository, reloadKey) {
        value = withContext(Dispatchers.IO) {
            repository.loadSnapshot()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppDestination.Dashboard.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Burger King Hiring") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppColors.TopBar,
                    titleContentColor = AppColors.TopBarText,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = AppColors.Navigation) {
                AppDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        if (snapshotState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = AppDestination.Dashboard.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(AppDestination.Dashboard.route) {
                    DashboardScreen(
                        snapshot = snapshotState!!.dashboard,
                        onOpenVacancies = {
                            navController.navigate(AppDestination.Vacancies.route)
                        },
                        onOpenCandidates = {
                            navController.navigate(AppDestination.Candidates.route)
                        },
                    )
                }
                composable(AppDestination.Vacancies.route) {
                    VacanciesScreen(
                        vacancies = snapshotState!!.vacancies,
                        onCyclePriority = { vacancyId ->
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    repository.cycleVacancyPriority(vacancyId)
                                }
                                reloadKey++
                            }
                        },
                        onToggleStatus = { vacancyId ->
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    repository.toggleVacancyStatus(vacancyId)
                                }
                                reloadKey++
                            }
                        },
                    )
                }
                composable(AppDestination.Candidates.route) {
                    CandidatesScreen(
                        candidates = snapshotState!!.candidates,
                        vacancies = snapshotState!!.vacancies.filter { it.status == "Открыта" },
                        onAddCandidate = { draft: CandidateDraft ->
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    repository.addCandidate(draft)
                                }
                                reloadKey++
                            }
                        },
                        onAdvanceStage = { candidateId ->
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    repository.advanceCandidateStage(candidateId)
                                }
                                reloadKey++
                            }
                        },
                        onRescheduleInterview = { candidateId ->
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    repository.rescheduleCandidateInterview(candidateId)
                                }
                                reloadKey++
                            }
                        },
                    )
                }
            }
        }
    }
}
