package com.mypayroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.mypayroll.core.di.AppContainer
import com.mypayroll.core.di.LocalAppContainer
import com.mypayroll.presentation.navigation.AppNavGraph
import com.mypayroll.presentation.theme.MyPayrollTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = AppContainer.getInstance(applicationContext)
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                MyPayrollTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}
