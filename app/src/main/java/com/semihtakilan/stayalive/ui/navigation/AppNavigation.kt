package com.semihtakilan.stayalive.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.semihtakilan.stayalive.R
import com.semihtakilan.stayalive.ui.home.HomeScreen
import com.semihtakilan.stayalive.ui.insights.InsightsScreen
import com.semihtakilan.stayalive.ui.settings.SettingsScreen
import com.semihtakilan.stayalive.ui.viewmodel.HomeViewModel
import com.semihtakilan.stayalive.ui.viewmodel.InsightsViewModel
import com.semihtakilan.stayalive.ui.viewmodel.SettingsViewModel
import com.semihtakilan.stayalive.ui.viewmodel.StayAliveViewModelFactory

object AppRoutes {
    const val HOME = "home"
    const val INSIGHTS = "insights"
    const val SETTINGS = "settings"
}

private data class BottomDestination(
    val route: String,
    val labelRes: Int,
    val contentDescriptionRes: Int,
    val icon: ImageVector,
)

@Composable
fun StayAliveAppNavHost(
    viewModelFactory: StayAliveViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val destinations = listOf(
        BottomDestination(
            route = AppRoutes.HOME,
            labelRes = R.string.nav_home,
            contentDescriptionRes = R.string.content_desc_nav_home,
            icon = Icons.Filled.Home,
        ),
        BottomDestination(
            route = AppRoutes.INSIGHTS,
            labelRes = R.string.nav_insights,
            contentDescriptionRes = R.string.content_desc_nav_insights,
            icon = Icons.Filled.BarChart,
        ),
        BottomDestination(
            route = AppRoutes.SETTINGS,
            labelRes = R.string.nav_settings,
            contentDescriptionRes = R.string.content_desc_nav_settings,
            icon = Icons.Filled.Settings,
        ),
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val current = backStack?.destination
            NavigationBar {
                destinations.forEach { dest ->
                    val selected = current?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = stringResource(dest.contentDescriptionRes),
                            )
                        },
                        label = { Text(stringResource(dest.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoutes.HOME) {
                val vm: HomeViewModel = viewModel(factory = viewModelFactory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    state = state,
                    onOpenAdd = vm::openAddSheet,
                    onOpenEdit = vm::openEditSheet,
                    onDismissSheet = vm::dismissAddSheet,
                    onFieldChange = vm::setActiveField,
                    onDigit = vm::appendDigit,
                    onDeleteDigit = vm::deleteLastDigit,
                    onDeleteMeasurement = vm::deleteEditedMeasurement,
                    onToggleTag = vm::toggleTagKey,
                    onDraftTimestampChange = vm::setDraftTimestampMillis,
                    onClearSaveError = vm::clearSaveError,
                    onSave = vm::trySaveDraft,
                )
            }
            composable(AppRoutes.INSIGHTS) {
                val vm: InsightsViewModel = viewModel(factory = viewModelFactory)
                val state by vm.uiState.collectAsStateWithLifecycle()
                InsightsScreen(
                    state = state,
                    onExportCsv = vm::exportCsvAndShare,
                )
            }
            composable(AppRoutes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = viewModelFactory)
                val prefs by vm.preferences.collectAsStateWithLifecycle()
                SettingsScreen(
                    preferences = prefs,
                    onAgeChange = vm::setAge,
                    onWeightKgChange = vm::setWeightKg,
                    onHeightCmChange = vm::setHeightCm,
                    onLanguageChange = vm::setLanguagePref,
                    onThemeChange = vm::setThemePref,
                    onUnitChange = vm::setUnitPref,
                )
            }
        }
    }
}
