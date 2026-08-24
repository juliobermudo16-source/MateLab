package com.matelab.islas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.matelab.islas.core.MateLabApplication
import com.matelab.islas.ui.navigation.MateLabApp

/**
 * Unica Activity de MateLab. Toda la interfaz es Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val container = (application as MateLabApplication).container

        setContent {
            MateLabApp(container = container)
        }
    }
}
