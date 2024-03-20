package com.andforce.socket

import com.andforce.network.DowloadStatus

interface MouseEventListener {

    fun onDown(mouseEvent: MouseEvent)
    fun onMove(mouseEvent: MouseEvent)
    fun onUp(mouseEvent: MouseEvent)
}

interface ApkEventListener {
    fun onApk(apkName: ApkEvent)
}

interface SocketStatusListener {
    fun onStatus(status: SocketStatus)

    sealed class SocketStatus {
        data object CONNECTING : SocketStatus()
        data object CONNECTED : SocketStatus()
        data object DISCONNECTED: SocketStatus()
        data object CONNECT_ERROR: SocketStatus()
    }
}