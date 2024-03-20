package com.andforce.socket

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class SocketEventRepository {

    suspend fun listenMouseEventFromSocket(socketClient: SocketClient): Flow<MouseEvent> = callbackFlow {
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

    suspend fun listenApkFilePushEvent(socketClient: SocketClient): Flow<ApkEvent> = callbackFlow {
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

    suspend fun listenSocketStatus(socketClient: SocketClient): Flow<SocketStatusListener.SocketStatus> = callbackFlow {
        val listener = object : SocketStatusListener {

            override fun onStatus(status: SocketStatusListener.SocketStatus) {
                trySend(status)
            }
        }
        socketClient.registerSocketStatusListener(listener)
        awaitClose {
            socketClient.unregisterSocketStatusListener()
        }
    }
}