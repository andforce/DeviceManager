package com.andforce.network.download

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import java.io.File


class DownloaderViewModel : ViewModel() {


    private val _stateFlow = MutableStateFlow<File?>(null)
    val fileDownloadStateFlow: Flow<File> = _stateFlow.filter { it != null }.mapNotNull { it }

    private val _downloadProcessFlow = MutableLiveData(0f)
    val downloadProcessFlow: LiveData<Float> = _downloadProcessFlow

    fun downloadApk(context: Context, url: String) {
        viewModelScope.launch {
            FileDownloader.download(context, url) {
                success {
                    _stateFlow.value = it
                }
                process { _, _, process ->
                    _downloadProcessFlow.value = process
                }
                error {
                    Log.e("NetworkViewModel", "download error: $it")
                }
            }
        }
    }
}