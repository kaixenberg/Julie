package our.bunny.julie.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import our.bunny.julie.ui.screens.home.HomeScreen
import our.bunny.julie.ui.screens.pet.AddEditPetScreen
import our.bunny.julie.ui.screens.dashboard.DashboardScreen
import our.bunny.julie.ui.screens.weight.WeightTrackerScreen
import our.bunny.julie.ui.screens.water.WaterTrackerScreen
import our.bunny.julie.ui.screens.feeding.FeedingLogScreen
import our.bunny.julie.ui.screens.medication.MedicationListScreen
import our.bunny.julie.ui.screens.timeline.TimelineScreen

sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object AddEditPet : Screen("add_edit_pet_screen?petId={petId}") {
        fun passPetId(petId: Long? = null): String {
            return "add_edit_pet_screen?petId=${petId ?: -1L}"
        }
    }
    object Dashboard : Screen("dashboard_screen/{petId}") {
        fun passPetId(petId: Long): String = "dashboard_screen/$petId"
    }
    object WeightTracker : Screen("weight_tracker_screen/{petId}") {
        fun passPetId(petId: Long): String = "weight_tracker_screen/$petId"
    }
    object WaterTracker : Screen("water_tracker_screen/{petId}") {
        fun passPetId(petId: Long): String = "water_tracker_screen/$petId"
    }
    object FeedingLog : Screen("feeding_log_screen/{petId}") {
        fun passPetId(petId: Long): String = "feeding_log_screen/$petId"
    }
    object MedicationList : Screen("medication_list_screen/{petId}") {
        fun passPetId(petId: Long): String = "medication_list_screen/$petId"
    }
    object Timeline : Screen("timeline_screen/{petId}") {
        fun passPetId(petId: Long): String = "timeline_screen/$petId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToAddPet = {
                    navController.navigate(Screen.AddEditPet.route)
                },
                onNavigateToPetDetail = { petId ->
                    navController.navigate(Screen.Dashboard.passPetId(petId))
                }
            )
        }
        composable(
            route = Screen.AddEditPet.route,
            arguments = listOf(
                navArgument(name = "petId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            AddEditPetScreen(
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }
        composable(
            route = Screen.Dashboard.route,
            arguments = listOf(
                navArgument(name = "petId") {
                    type = NavType.LongType
                }
            )
        ) {
            DashboardScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEditPet = { petId -> navController.navigate(Screen.AddEditPet.passPetId(petId)) },
                onNavigateToWeightTracker = { petId -> navController.navigate(Screen.WeightTracker.passPetId(petId)) },
                onNavigateToWaterTracker = { petId -> navController.navigate(Screen.WaterTracker.passPetId(petId)) },
                onNavigateToFeedingLog = { petId -> navController.navigate(Screen.FeedingLog.passPetId(petId)) },
                onNavigateToMedicationList = { petId -> navController.navigate(Screen.MedicationList.passPetId(petId)) },
                onNavigateToTimeline = { petId -> navController.navigate(Screen.Timeline.passPetId(petId)) }
            )
        }
        composable(
            route = Screen.WeightTracker.route,
            arguments = listOf(
                navArgument(name = "petId") {
                    type = NavType.LongType
                }
            )
        ) {
            WeightTrackerScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = Screen.WaterTracker.route,
            arguments = listOf(
                navArgument(name = "petId") {
                    type = NavType.LongType
                }
            )
        ) {
            WaterTrackerScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = Screen.FeedingLog.route,
            arguments = listOf(
                navArgument(name = "petId") {
                    type = NavType.LongType
                }
            )
        ) {
            FeedingLogScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = Screen.MedicationList.route,
            arguments = listOf(
                navArgument(name = "petId") {
                    type = NavType.LongType
                }
            )
        ) {
            MedicationListScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
        composable(
            route = Screen.Timeline.route,
            arguments = listOf(
                navArgument(name = "petId") {
                    type = NavType.LongType
                }
            )
        ) {
            TimelineScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
