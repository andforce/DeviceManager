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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordViewModel :ViewModel(){

    private val recordRepository = RecordRepository()

    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage: StateFlow<Bitmap?> get() = _capturedImage

    private val _recordState = MutableStateFlow<RecordState>(RecordState.Stopped)
    val recordState: LiveData<RecordState> = _recordState.asLiveData()

    fun startCaptureImages(context: Context, mp: MediaProjection, scale: Float) {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        viewModelScope.launch(handler) {
            _recordState.tryEmit(RecordState.Recording)
            recordRepository.captureBitmap(context.applicationContext, mp, scale).collectLatest {
                _capturedImage.value = it
            }
        }

        viewModelScope.launch {
            recordRepository.listenCapture().collect {
                _recordState.tryEmit(it)
            }
        }
    }

}