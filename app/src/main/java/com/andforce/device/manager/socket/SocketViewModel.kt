package com.andforce.device.manager.socket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andforce.socket.ApkEvent
import com.andforce.socket.MouseEvent
import com.andforce.socket.SocketClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SocketViewModel : ViewModel() {
    private val socketRepository = SocketRepository()

    private val _apkEventFlow = MutableSharedFlow<ApkEvent?>(replay = 0)
    var apkEventFlow: SharedFlow<ApkEvent?> = _apkEventFlow


    private val _eventFlow = MutableSharedFlow<MouseEvent?>(replay = 0)
    var eventFlow: SharedFlow<MouseEvent?> = _eventFlow

    fun listenEvent(socketClient: SocketClient) {
        viewModelScope.launch {
            socketRepository.listenEvent(socketClient).buffer(1024).collect {
                _eventFlow.emit(it)
            }
        }
    }

    fun listenApkEvent(socketClient: SocketClient) {
        viewModelScope.launch {
            socketRepository.listenApkEvent(socketClient).buffer(1024).collectLatest {
                _apkEventFlow.emit(it)
            }
        }
    }
}