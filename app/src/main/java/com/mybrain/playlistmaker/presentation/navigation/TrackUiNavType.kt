package com.mybrain.playlistmaker.presentation.navigation

import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.util.Base64
import androidx.core.os.BundleCompat
import androidx.navigation.NavType
import com.mybrain.playlistmaker.presentation.entity.TrackUI

private const val BUNDLE_TRACK_KEY = "nav_track_ui"

val TrackUiNavType: NavType<TrackUI> =
    object : NavType<TrackUI>(isNullableAllowed = false) {
        override fun put(
            bundle: Bundle,
            key: String,
            value: TrackUI,
        ) {
            bundle.putParcelable(key, value)
        }

        override fun get(
            bundle: Bundle,
            key: String,
        ): TrackUI =
            BundleCompat.getParcelable(bundle, key, TrackUI::class.java)
                ?: error("Missing TrackUI argument")

        override fun parseValue(value: String): TrackUI {
            val decoded =
                Base64.decode(
                    Uri.decode(value),
                    Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
                )
            val parcel = Parcel.obtain()
            return try {
                parcel.unmarshall(decoded, 0, decoded.size)
                parcel.setDataPosition(0)
                val loader = TrackUI::class.java.classLoader
                val wrapper = parcel.readBundle(loader) ?: error("Invalid TrackUI parcel")
                wrapper.classLoader = loader
                BundleCompat.getParcelable(wrapper, BUNDLE_TRACK_KEY, TrackUI::class.java)
                    ?: error("Invalid TrackUI parcel")
            } finally {
                parcel.recycle()
            }
        }

        override fun serializeAsValue(value: TrackUI): String {
            val loader = TrackUI::class.java.classLoader
            val wrapper =
                Bundle().apply {
                    classLoader = loader
                    putParcelable(BUNDLE_TRACK_KEY, value)
                }
            val parcel = Parcel.obtain()
            return try {
                wrapper.writeToParcel(parcel, 0)
                val bytes = parcel.marshall()
                Uri.encode(
                    Base64.encodeToString(
                        bytes,
                        Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
                    ),
                )
            } finally {
                parcel.recycle()
            }
        }
    }
