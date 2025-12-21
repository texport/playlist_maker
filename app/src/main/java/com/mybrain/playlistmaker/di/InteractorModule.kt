package com.mybrain.playlistmaker.di

import com.mybrain.playlistmaker.domain.interactors.SearchHistoryInteractor
import com.mybrain.playlistmaker.domain.interactors.SearchInteractor
import com.mybrain.playlistmaker.domain.interactors.SettingsInteractor
import com.mybrain.playlistmaker.domain.interactors.impl.SearchHistoryInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.SearchInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.SettingsInteractorImpl
import org.koin.dsl.module

val interactorModule = module {
    single<SettingsInteractor> { SettingsInteractorImpl(get()) }
    single<SearchInteractor> { SearchInteractorImpl(get()) }
    single<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }
}