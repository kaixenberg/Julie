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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import our.bunny.julie.domain.model.ThemeConfig
import our.bunny.julie.domain.repository.SettingsRepository
import our.bunny.julie.ui.theme.LocalBlurEnabled
import our.bunny.julie.ui.theme.LocalPredictiveBackEnabled
import javax.inject.Inject


// ─── Debug flag ───────────────────────────────────────────────────────────────
// Set to true to launch ThemePreviewScreen instead of the normal app.
// Flip this to false (or delete the block) once you've confirmed the theme looks right.
private const val DEBUG_SHOW_THEME_PREVIEW = false

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            if (DEBUG_SHOW_THEME_PREVIEW) {
                // Launches the self-contained debug preview — no NavGraph needed.
                our.bunny.julie.ui.screens.debug.ThemePreviewScreen()
            } else {
                // Production path: params read from DataStore
                val themeConfig by settingsRepository.themeConfigFlow.collectAsState(initial = ThemeConfig.SYSTEM)
                val dynamicColor by settingsRepository.dynamicColorFlow.collectAsState(initial = true)
                val paletteStyle by settingsRepository.paletteStyleFlow.collectAsState(initial = PaletteStyle.TonalSpot)
                val predictiveBack by settingsRepository.predictiveBackFlow.collectAsState(initial = true)
                val blurEffects by settingsRepository.blurEffectsFlow.collectAsState(initial = false)

                val isDark = when (themeConfig) {
                    ThemeConfig.DARK -> true
                    ThemeConfig.LIGHT -> false
                    ThemeConfig.SYSTEM -> isSystemInDarkTheme()
                }

                CompositionLocalProvider(
                    LocalBlurEnabled provides blurEffects,
                    LocalPredictiveBackEnabled provides predictiveBack
                ) {
                    JulieTheme(
                        darkTheme = isDark,
                        dynamicColor = dynamicColor,
                        paletteStyle = paletteStyle,
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
}
