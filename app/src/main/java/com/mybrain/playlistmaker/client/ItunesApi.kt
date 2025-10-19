package com.mybrain.playlistmaker.client

import com.mybrain.playlistmaker.dto.ItunesSearchResponseDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search")
    fun searchTracks(
        @Query("term") term: String,
        @Query("entity") entity: String = "song"
    ): Call<ItunesSearchResponseDto>
}