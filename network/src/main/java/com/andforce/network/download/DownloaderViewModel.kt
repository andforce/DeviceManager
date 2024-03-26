package com.andforce.network.download

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import java.io.File


class DownloaderViewModel : ViewModel() {

    private val _downloadFileFlow = MutableStateFlow<File?>(null)
    val downloadStateFlow: Flow<File> = _downloadFileFlow.filter { it != null }.mapNotNull { it }

    private val _downloadProcessFlow = MutableLiveData(0f)
    val downloadProcessFlow: LiveData<Float> = _downloadProcessFlow

    private val _downloadError = MutableStateFlow<DownloadError?>(null)
    val downloadErrorFlow: StateFlow<DownloadError?> = _downloadError

    suspend fun downloadApk(context: Context, url: String) {
        FileDownloader.download(context, url) {
            onSuccess {
                _downloadFileFlow.value = it
            }
            onProcess { _, _, process ->
                _downloadProcessFlow.value = process
            }
            onError {
                _downloadError.value = it
            }
        }
    }
}