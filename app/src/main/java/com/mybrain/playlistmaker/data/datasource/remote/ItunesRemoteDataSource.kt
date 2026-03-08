package com.mybrain.playlistmaker.data.datasource.remote

import com.mybrain.playlistmaker.data.dto.ItunesSearchRequestDto
import com.mybrain.playlistmaker.data.dto.ItunesSearchResponseDto
import com.mybrain.playlistmaker.data.network.api.ItunesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ItunesRemoteDataSource(private val api: ItunesApi) {
    suspend fun search(request: ItunesSearchRequestDto): Result<ItunesSearchResponseDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.search(request.term)
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
