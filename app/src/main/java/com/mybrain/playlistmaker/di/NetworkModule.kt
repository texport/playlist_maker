package com.mybrain.playlistmaker.di

import com.mybrain.playlistmaker.data.network.api.ItunesApi
import com.mybrain.playlistmaker.data.network.client.RetrofitServiceFactory
import org.koin.dsl.module

private const val ITUNES_BASE_URL = "https://itunes.apple.com/"

val networkModule = module {
    single<ItunesApi> {
        RetrofitServiceFactory.createApi<ItunesApi>(ITUNES_BASE_URL)
    }
}