package com.andforce.screen.cast.coroutine

import android.content.Context
import android.graphics.Bitmap
import android.media.projection.MediaProjection
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.andforce.screen.cast.listener.RecordState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordViewModel :ViewModel(){

    private val recordRepository = RecordRepository()

    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImageFlow: StateFlow<Bitmap?> get() = _capturedImage

    private val _recordState = MutableStateFlow<RecordState>(RecordState.Stopped)
    val recordState: LiveData<RecordState> = _recordState.asLiveData()

    fun startCaptureImages(context: Context, mp: MediaProjection, scale: Float) {
        viewModelScope.launch {
            recordRepository.recordStatusFlow().collect {
                _recordState.tryEmit(it)
            }
        }

        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        viewModelScope.launch(handler) {
            _recordState.tryEmit(RecordState.Recording)
            recordRepository.captureBitmapFlow(context.applicationContext, mp, scale).collectLatest {
                _capturedImage.value = it
            }
        }
    }
}