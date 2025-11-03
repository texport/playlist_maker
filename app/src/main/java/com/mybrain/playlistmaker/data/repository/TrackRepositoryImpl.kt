package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.datasource.remote.ItunesRemoteDataSource
import com.mybrain.playlistmaker.data.mappers.toDomain
import com.mybrain.playlistmaker.data.mappers.toRequestDto
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams
import com.mybrain.playlistmaker.domain.repository.TrackRepository

class TrackRepositoryImpl(
    private val remoteDataSource: ItunesRemoteDataSource
) : TrackRepository {
    override fun search(params: TrackSearchParams, onResult: (response: Result<List<TrackDomain>>) -> Unit) {
        val requestDto = params.toRequestDto()

        remoteDataSource.search(requestDto) { result ->
            result.onSuccess { response ->
                val tracks = response.results.map { it.toDomain() }
                onResult(Result.success(tracks))
            }.onFailure { error ->
                onResult(Result.failure(error))
            }
        }
    }
}