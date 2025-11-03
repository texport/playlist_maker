package com.mybrain.playlistmaker.data.datasource.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mybrain.playlistmaker.data.dto.TrackLocalDto

class SearchHistoryLocalDataSource(
    private val prefs: PrefsLocalDataSource,
    private val gson: Gson
) {
    private val type = object : TypeToken<ArrayList<TrackLocalDto>>() {}.type

    fun get(): ArrayList<TrackLocalDto> {
        val json = prefs.getString(KEY) ?: return arrayListOf()
        return runCatching { gson.fromJson<ArrayList<TrackLocalDto>>(json, type) }.getOrElse { arrayListOf() }
    }

    fun clear() = prefs.remove(KEY)

    fun add(track: TrackLocalDto): ArrayList<TrackLocalDto> {
        val list = get()
        val i = list.indexOfFirst { it.trackId == track.trackId }
        if (i >= 0) list.removeAt(i)
        list.add(0, track)
        while (list.size > MAX) list.removeAt(list.size - 1)
        prefs.putString(KEY, gson.toJson(list))
        return list
    }

    companion object {
        private const val KEY = "search_history_tracks"
        private const val MAX = 10
    }
}