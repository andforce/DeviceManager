package com.andforce.screen.cast.coroutine

import android.content.Context
import android.media.projection.MediaProjection
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.andforce.screen.cast.listener.RecordState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScreenCastViewModel : ViewModel() {

    private val recordRepository = ScreenCastRepository()

    private val _capturedImageFlow = MutableStateFlow<ByteArray?>(null)
    val capturedImageFlow: StateFlow<ByteArray?> = _capturedImageFlow

    private val _recordState = MutableStateFlow<RecordState>(RecordState.Stopped)
    val recordState: LiveData<RecordState> = _recordState.asLiveData()

    suspend fun startCollectCaptureStatue() {
        Log.i("CAPTURE", "startCollectCaptureStatue()")
        recordRepository.recordStatusFlow().collect {
            _recordState.tryEmit(it)
        }
    }
    suspend fun startCaptureImages(context: Context, mp: MediaProjection, scale: Float) {
        Log.i("CAPTURE", "startCaptureImages()")
        _recordState.tryEmit(RecordState.Recording)
        recordRepository.captureBitmapFlow(context.applicationContext, mp, scale)
            .collectLatest {
                Log.i("CAPTURE", "emit captured image")
                _capturedImageFlow.value = it
            }
    }
}