package com.mybrain.playlistmaker.di

import android.media.MediaPlayer
import org.koin.dsl.module

val playerModule = module {
    factory { MediaPlayer() }
}