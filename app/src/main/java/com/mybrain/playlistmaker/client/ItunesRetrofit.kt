package com.mybrain.playlistmaker.client

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ItunesRetrofit {
    private const val BASE_URL = "https://itunes.apple.com/"

    val api: ItunesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }
}