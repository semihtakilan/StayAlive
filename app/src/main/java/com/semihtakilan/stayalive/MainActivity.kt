package com.semihtakilan.stayalive

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.semihtakilan.stayalive.ui.navigation.StayAliveAppNavHost
import com.semihtakilan.stayalive.ui.theme.ProvideAppLocale
import com.semihtakilan.stayalive.ui.theme.StayAliveTheme
import com.semihtakilan.stayalive.ui.viewmodel.MainViewModel
import com.semihtakilan.stayalive.ui.viewmodel.StayAliveViewModelFactory

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as StayAliveApplication

        setContent {
            val factory = remember(app) {
                StayAliveViewModelFactory(app.container, app.bootstrapPreferences)
            }
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            val prefs by mainViewModel.preferences.collectAsStateWithLifecycle()

            ProvideAppLocale(prefs.languagePref) {
                StayAliveTheme(
                    themePref = prefs.themePref,
                    darkTheme = isSystemInDarkTheme(),
                ) {
                    StayAliveAppNavHost(
                        viewModelFactory = factory,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
