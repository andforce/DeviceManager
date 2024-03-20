package com.andforce.socket

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class SocketRepository {

    suspend fun listenEvent(socketClient: SocketClient): Flow<MouseEvent> = callbackFlow {
        val listener = object : MouseEventListener {
            override fun onDown(mouseEvent: MouseEvent) {
                trySend(mouseEvent)
            }

            override fun onUp(mouseEvent: MouseEvent) {
                trySend(mouseEvent)
            }

            override fun onMove(mouseEvent: MouseEvent) {
                trySend(mouseEvent)
            }
        }
        socketClient.registerMouseEventListener(listener)
        awaitClose {
            socketClient.unRegisterMouseEventListener()
        }
    }

    suspend fun listenApkEvent(socketClient: SocketClient): Flow<ApkEvent> = callbackFlow {
        val listener = object : ApkEventListener {
            override fun onApk(apkName: ApkEvent) {
                trySend(apkName)
            }
        }
        socketClient.registerApkEventListener(listener)
        awaitClose {
            socketClient.unRegisterApkEventListener()
        }
    }
}