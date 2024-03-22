package com.andforce.screen.cast.coroutine

import android.content.Context
import android.media.projection.MediaProjection
import kotlinx.coroutines.flow.Flow

class RecordRepository {

    private val recordDataSource = RecordDataSource()

    suspend fun captureBitmapFlow(context: Context, mp: MediaProjection, scale: Float): Flow<ByteArray> =
        recordDataSource.captureImageFlow(context, mp, scale)
    suspend fun recordStatusFlow() = recordDataSource.recordStatusFlow()
}