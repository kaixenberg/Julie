package our.bunny.julie.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import our.bunny.julie.ui.screens.feeding.FeedingLogScreen
import our.bunny.julie.ui.screens.medication.MedicationListScreen
import our.bunny.julie.ui.screens.water.WaterTrackerScreen
import our.bunny.julie.ui.screens.weight.WeightTrackerScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Licenses : Screen("licenses")
    object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: String) = "pet_detail/$petId"
    }
    object PetStatDetail : Screen("pet_stat_detail/{petId}/{statType}") {
        fun createRoute(petId: String, statType: StatType) = "pet_stat_detail/$petId/${statType.name}"
    }
    object AddEditPet : Screen("add_edit_pet/{petId}") {
        fun createRoute(petId: Long = -1L) = "add_edit_pet/$petId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController, onOpenDrawer: () -> Unit) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(400)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(400)) }
    ) {
        composable(Screen.Dashboard.route) {
            JulieAppScaffold(
                title = "Dashboard", 
                onOpenDrawer = onOpenDrawer,
                floatingActionButton = {
                    androidx.compose.material3.FloatingActionButton(onClick = { navController.navigate(Screen.AddEditPet.createRoute()) }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Add, 
                            contentDescription = "Add Pet"
                        )
                    }
                }
            ) { innerPadding ->
                our.bunny.julie.ui.screens.dashboard.DashboardScreen(
                    paddingValues = innerPadding,
                    onNavigateToAddPet = {
                        navController.navigate(Screen.AddEditPet.createRoute())
                    },
                    onNavigateToPetDetail = { petId ->
                        navController.navigate(Screen.PetDetail.createRoute(petId.toString()))
                    },
                    onNavigateToPetStatDetail = { petId, statType ->
                        navController.navigate(Screen.PetStatDetail.createRoute(petId.toString(), statType))
                    }
                )
            }
        }
        composable(
            route = Screen.AddEditPet.route,
            arguments = listOf(navArgument("petId") { type = NavType.LongType; defaultValue = -1L })
        ) {
            JulieAppScaffold(title = "Add/Edit Pet", onOpenDrawer = onOpenDrawer) { innerPadding ->
                our.bunny.julie.ui.screens.pet.AddEditPetScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }
        }
        composable(Screen.Settings.route) {
            JulieAppScaffold(title = "Settings", onOpenDrawer = onOpenDrawer) { innerPadding ->
                our.bunny.julie.ui.screens.settings.SettingsScreen(
                    paddingValues = innerPadding,
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }
        }
        composable(Screen.About.route) {
            our.bunny.julie.ui.screens.about.AboutScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToLicenses = { navController.navigate(Screen.Licenses.route) }
            )
        }
        composable(Screen.Licenses.route) {
            JulieAppScaffold(title = "Open Source Licenses", onOpenDrawer = onOpenDrawer) { innerPadding ->
                androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                    com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        composable(
            route = Screen.PetDetail.route,
            arguments = listOf(navArgument("petId") { type = NavType.LongType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getLong("petId") ?: -1L
            our.bunny.julie.ui.screens.pet.PetDetailScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToEditPet = { id -> navController.navigate(Screen.AddEditPet.createRoute(id)) },
                onNavigateToWeightTracker = { id -> navController.navigate(Screen.PetStatDetail.createRoute(id.toString(), StatType.Weight)) },
                onNavigateToWaterTracker = { id -> navController.navigate(Screen.PetStatDetail.createRoute(id.toString(), StatType.Water)) },
                onNavigateToFeedingLog = { id -> navController.navigate(Screen.PetStatDetail.createRoute(id.toString(), StatType.Feeding)) },
                onNavigateToMedicationList = { id -> navController.navigate(Screen.PetStatDetail.createRoute(id.toString(), StatType.Medication)) },
                onNavigateToTimeline = { id -> navController.navigate(Screen.PetStatDetail.createRoute(id.toString(), StatType.Timeline)) }
            )
        }
        composable(
            route = Screen.PetStatDetail.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.LongType },
                navArgument("statType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getLong("petId") ?: -1L
            val statTypeStr = backStackEntry.arguments?.getString("statType")
            
            // Thin dispatcher based on statType
            when (statTypeStr) {
                StatType.Weight.name -> {
                    WeightTrackerScreen(onNavigateUp = { navController.navigateUp() })
                }
                StatType.Water.name -> {
                    WaterTrackerScreen(onNavigateUp = { navController.navigateUp() })
                }
                StatType.Feeding.name -> {
                    FeedingLogScreen(onNavigateUp = { navController.navigateUp() })
                }
                StatType.Medication.name -> {
                    MedicationListScreen(onNavigateUp = { navController.navigateUp() })
                }
                StatType.Timeline.name -> {
            our.bunny.julie.ui.screens.timeline.TimelineScreen(onNavigateUp = { navController.navigateUp() })
        }
        else -> {
                    JulieAppScaffold(title = "$statTypeStr", onOpenDrawer = onOpenDrawer) { innerPadding ->
                        PlaceholderScreen("Pet Stat Detail - ID: $petId, Stat: $statTypeStr", innerPadding)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String, paddingValues: androidx.compose.foundation.layout.PaddingValues) {
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
        Text(text = text)
    }
}
