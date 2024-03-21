package com.andforce.screen.cast.coroutine

import android.content.Context
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class RecordRepository {

    private val recordDataSource = RecordDataSource()

    suspend fun captureImageFlow(context: Context, mp: MediaProjection, scale: Float) =
        recordDataSource.captureImageFlow(context, mp, scale)

    suspend fun captureBitmapFlow(context: Context, mp: MediaProjection, scale: Float): Flow<Bitmap> =
        recordDataSource.captureImageFlow(context, mp, scale).mapNotNull { image ->
            val width = image.width
            val height = image.height

            val plane = image.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * width

            val orgBitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride,
                height,
                Bitmap.Config.ARGB_8888
            ).apply {
                copyPixelsFromBuffer(buffer)
            }

            Bitmap.createBitmap(orgBitmap, 0, 0, width, height).also {
                if (!orgBitmap.isRecycled) {
                    orgBitmap.recycle()
                }
                image.close()
            }
        }
    suspend fun recordStatusFlow() = recordDataSource.recordStatusFlow()
}