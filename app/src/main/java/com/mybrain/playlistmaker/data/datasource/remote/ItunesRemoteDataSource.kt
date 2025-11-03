package com.mybrain.playlistmaker.data.datasource.remote

import com.mybrain.playlistmaker.data.dto.ItunesSearchRequestDto
import com.mybrain.playlistmaker.data.dto.ItunesSearchResponseDto
import com.mybrain.playlistmaker.data.network.api.ItunesApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ItunesRemoteDataSource(private val api: ItunesApi) {
    fun search(request: ItunesSearchRequestDto, result: (Result<ItunesSearchResponseDto>) -> Unit) {
        api.search(request.term).enqueue(object : Callback<ItunesSearchResponseDto> {
            override fun onResponse(
                call: Call<ItunesSearchResponseDto>,
                response: Response<ItunesSearchResponseDto>
            ) {
                val body = response.body()
                if (response.isSuccessful && body != null) result(Result.success(body))
                else result(Result.failure(Exception("HTTP ${response.code()} or empty body")))
            }
            override fun onFailure(call: Call<ItunesSearchResponseDto>, t: Throwable) {
                result(Result.failure(t))
            }
        })
    }
}