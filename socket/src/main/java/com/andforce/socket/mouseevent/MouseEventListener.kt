package com.andforce.socket.mouseevent

import com.andforce.socket.apkevent.ApkPushEvent

interface MouseEventListener {

    fun onDown(mouseEvent: MouseEvent)
    fun onMove(mouseEvent: MouseEvent)
    fun onUp(mouseEvent: MouseEvent)
}

interface ApkEventListener {
    fun onApk(apkName: ApkPushEvent)
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