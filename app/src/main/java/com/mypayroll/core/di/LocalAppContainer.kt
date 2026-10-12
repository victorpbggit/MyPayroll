package com.mypayroll.core.di

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer not provided - wrap the composable tree with CompositionLocalProvider(LocalAppContainer provides ...)")
}
