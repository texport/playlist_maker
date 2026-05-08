package com.mybrain.playlistmaker.di

import com.mybrain.playlistmaker.presentation.navigation.NavigationEvents
import org.koin.dsl.module

val navigationModule =
    module {
        single { NavigationEvents() }
    }
