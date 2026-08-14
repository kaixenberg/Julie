package our.bunny.julie.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import our.bunny.julie.domain.model.Pet
import our.bunny.julie.ui.screens.pet.PetData

@Composable
fun JulieAppDrawer(
    drawerState: DrawerState,
    navController: NavController,
    pets: List<Pet>,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBlurEnabled = our.bunny.julie.ui.theme.LocalBlurEnabled.current
    val blurRadius by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (drawerState.targetValue == androidx.compose.material3.DrawerValue.Open && isBlurEnabled) 16.dp else 0.dp
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Text(
                    text = "Julie",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(28.dp)
                )
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = currentRoute?.startsWith("dashboard") == true,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        if (currentRoute?.startsWith("dashboard") != true) {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                pets.forEach { pet ->
                    NavigationDrawerItem(
                        icon = { 
                            Text(
                                text = PetData.getEmojiForSpecies(pet.species),
                                style = MaterialTheme.typography.titleMedium
                            ) 
                        },
                        label = { Text(pet.name) },
                        selected = currentRoute?.startsWith("pet_detail/${pet.id}") == true,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            if (currentRoute?.startsWith("pet_detail/${pet.id}") != true) {
                                navController.navigate("pet_detail/${pet.id}") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Add Pet") },
                    selected = currentRoute?.startsWith("add_edit_pet") == true,
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        if (currentRoute?.startsWith("add_edit_pet") != true) {
                            navController.navigate("add_edit_pet/-1") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        coroutineScope.launch { drawerState.close() }
                        if (currentRoute != "settings") {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        },
        content = {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.blur(blurRadius)
            ) {
                content()
            }
        }
    )
}
