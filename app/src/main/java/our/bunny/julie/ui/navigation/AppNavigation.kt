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

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
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
            JulieAppScaffold(title = "Dashboard", onOpenDrawer = onOpenDrawer) { innerPadding ->
                our.bunny.julie.ui.screens.dashboard.DashboardScreen(
                    paddingValues = innerPadding,
                    onNavigateToAddPet = {
                        navController.navigate(Screen.AddEditPet.createRoute())
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
                PlaceholderScreen("Settings", innerPadding)
            }
        }
        composable(
            route = Screen.PetDetail.route,
            arguments = listOf(navArgument("petId") { type = NavType.StringType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            JulieAppScaffold(title = "Pet Detail", onOpenDrawer = onOpenDrawer) { innerPadding ->
                PlaceholderScreen("Pet Detail - ID: $petId", innerPadding)
            }
        }
        composable(
            route = Screen.PetStatDetail.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType },
                navArgument("statType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId")
            val statTypeStr = backStackEntry.arguments?.getString("statType")
            JulieAppScaffold(title = "$statTypeStr", onOpenDrawer = onOpenDrawer) { innerPadding ->
                PlaceholderScreen("Pet Stat Detail - ID: $petId, Stat: $statTypeStr", innerPadding)
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
