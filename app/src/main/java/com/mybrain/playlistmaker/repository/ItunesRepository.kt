package com.mybrain.playlistmaker.repository

import com.mybrain.playlistmaker.client.ItunesRetrofit
import com.mybrain.playlistmaker.dto.ItunesSearchResponseDto
import com.mybrain.playlistmaker.mappers.toDomain
import com.mybrain.playlistmaker.models.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItunesRepository {
    fun search(term: String, onResult: (response: Result<List<Track>>) -> Unit) {
        ItunesRetrofit.api.searchTracks(term)
            .enqueue(object : Callback<ItunesSearchResponseDto> {
                override fun onResponse(
                    call: Call<ItunesSearchResponseDto>,
                    response: Response<ItunesSearchResponseDto>
                ) {
                    if (!response.isSuccessful) {
                        onResult(Result.failure(Exception("HTTP ${response.code()}")))
                        return
                    }

                    val body = response.body() ?: return onResult(Result.failure(Exception("Response body is null")))
                    val tracks = body.results.map { it.toDomain() }
                    onResult(Result.success(tracks))
                }

                override fun onFailure(
                    call: Call<ItunesSearchResponseDto>,
                    t: Throwable
                ) {
                    onResult(Result.failure(t))
                }
            })
    }
}