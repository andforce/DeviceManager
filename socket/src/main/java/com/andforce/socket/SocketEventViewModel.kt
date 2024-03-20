package com.andforce.socket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SocketEventViewModel : ViewModel() {
    private val socketEventRepository = SocketEventRepository()

    private val _apkEventFlow = MutableSharedFlow<ApkEvent?>(replay = 0)
    var apkEventFlow: SharedFlow<ApkEvent?> = _apkEventFlow


    private val _eventFlow = MutableSharedFlow<MouseEvent?>(replay = 0)
    var eventFlow: SharedFlow<MouseEvent?> = _eventFlow

    fun listenEvent(socketClient: SocketClient) {
        viewModelScope.launch {
            socketEventRepository.listenEvent(socketClient).buffer(1024).collect {
                _eventFlow.emit(it)
            }
        }
    }

    fun listenApkEvent(socketClient: SocketClient) {
        viewModelScope.launch {
            socketEventRepository.listenApkEvent(socketClient).buffer(1024).collectLatest {
                _apkEventFlow.emit(it)
            }
        }
    }
}