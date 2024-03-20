package com.cry.screenop.coroutine

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecordViewModel(private val scope: CoroutineScope) {

    private val repo: RecordRepository = RecordRepository()

    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage: StateFlow<Bitmap?> get() = _capturedImage

    private val _recordState = MutableLiveData<RecordState>(RecordState.Stopped)
    val recordState: MutableLiveData<RecordState> get() = _recordState

    fun startCaptureImages(context: Context, mp: MediaProjection, scale: Float) {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        scope.launch(handler) {
            repo.captureBitmap(context.applicationContext, mp, scale).collect() {
                _capturedImage.value = it
            }
        }
    }

    fun updateRecordState(state: RecordState) {
        _recordState.value = state
    }

    sealed class RecordState {
        data object Recording : RecordState()
        data object Stopped : RecordState()
    }

}