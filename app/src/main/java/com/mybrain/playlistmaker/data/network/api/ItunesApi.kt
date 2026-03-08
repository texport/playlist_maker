package com.mybrain.playlistmaker.data.network.api

import com.mybrain.playlistmaker.data.dto.ItunesSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("entity") entity: String = "song"
    ): ItunesSearchResponseDto
}
