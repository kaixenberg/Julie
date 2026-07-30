package our.bunny.julie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import our.bunny.julie.ui.theme.JulieTheme
import our.bunny.julie.ui.theme.PaletteStyle
import kotlinx.coroutines.launch
import our.bunny.julie.ui.screens.home.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue


// ─── Debug flag ───────────────────────────────────────────────────────────────
// Set to true to launch ThemePreviewScreen instead of the normal app.
// Flip this to false (or delete the block) once you've confirmed the theme looks right.
private const val DEBUG_SHOW_THEME_PREVIEW = false

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            if (DEBUG_SHOW_THEME_PREVIEW) {
                // Launches the self-contained debug preview — no NavGraph needed.
                our.bunny.julie.ui.screens.debug.ThemePreviewScreen()
            } else {
                // Production path: params are hardcoded here until the Settings screen
                // is built (Phase 9). Swap paletteStyle / seedColor freely to preview.
                JulieTheme(
                    dynamicColor = true,                    // wallpaper seed on API 27+
                    paletteStyle = PaletteStyle.TonalSpot,  // change to taste
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()
                        val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
                        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
                        
                        val homeViewModel: our.bunny.julie.ui.screens.home.HomeViewModel = hiltViewModel()
                        val pets by homeViewModel.pets.collectAsState()

                        our.bunny.julie.ui.navigation.JulieAppDrawer(
                            drawerState = drawerState,
                            navController = navController,
                            pets = pets
                        ) {
                            our.bunny.julie.ui.navigation.AppNavigation(
                                navController = navController,
                                onOpenDrawer = {
                                    coroutineScope.launch { drawerState.open() }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
