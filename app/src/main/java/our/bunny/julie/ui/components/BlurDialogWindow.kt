package our.bunny.julie.ui.components

import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import our.bunny.julie.ui.theme.LocalBlurEnabled

@Composable
fun BlurDialogWindow() {
    val isBlurEnabled = LocalBlurEnabled.current
    val view = LocalView.current
    LaunchedEffect(view, isBlurEnabled) {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (window != null) {
            if (isBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply {
                    blurBehindRadius = 40
                }
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            }
        }
    }
}
