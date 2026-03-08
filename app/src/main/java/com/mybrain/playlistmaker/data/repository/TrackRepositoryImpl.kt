package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.datasource.remote.ItunesRemoteDataSource
import com.mybrain.playlistmaker.data.mappers.toDomain
import com.mybrain.playlistmaker.data.mappers.toRequestDto
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams
import com.mybrain.playlistmaker.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TrackRepositoryImpl(
    private val remoteDataSource: ItunesRemoteDataSource
) : TrackRepository {
    override fun search(params: TrackSearchParams): Flow<Result<List<TrackDomain>>> = flow {
        val requestDto = params.toRequestDto()
        val result = remoteDataSource.search(requestDto)

        result.onSuccess { response ->
            val tracks = response.results.map { it.toDomain() }
            emit(Result.success(tracks))
        }.onFailure { error ->
            emit(Result.failure(error))
        }
    }
}
