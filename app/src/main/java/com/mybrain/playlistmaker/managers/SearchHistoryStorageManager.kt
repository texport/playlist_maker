package com.mybrain.playlistmaker.managers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mybrain.playlistmaker.models.Track

class SearchHistoryStorageManager(
    private val prefs: PrefsManager,
    private val gson: Gson = Gson()
) {
    private val type = object : TypeToken<ArrayList<Track>>() {}.type

    fun get(): ArrayList<Track> {
        val json = prefs.getString(KEY) ?: return arrayListOf()
        return runCatching { gson.fromJson<ArrayList<Track>>(json, type) }.getOrElse { arrayListOf() }
    }

    fun clear() = prefs.remove(KEY)

    fun add(track: Track): ArrayList<Track> {
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