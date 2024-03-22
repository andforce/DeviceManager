package com.andforce.socket.viewmodel

import com.andforce.socket.apkevent.ApkPushEvent
import com.andforce.socket.mouseevent.ApkEventListener
import com.andforce.socket.mouseevent.MouseEvent
import com.andforce.socket.mouseevent.MouseEventListener
import com.andforce.socket.SocketClient
import com.andforce.socket.apkevent.ApkUninstallEvent
import com.andforce.socket.mouseevent.ApkUninstallListener
import com.andforce.socket.mouseevent.SocketStatusListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


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

    suspend fun listenApkFilePushEvent(socketClient: SocketClient): Flow<ApkPushEvent> = callbackFlow {
        val listener = object : ApkEventListener {
            override fun onApk(apkName: ApkPushEvent) {
                trySend(apkName)
            }
        }
        socketClient.registerApkEventListener(listener)
        awaitClose {
            socketClient.unRegisterApkEventListener()
        }
    }

    suspend fun listenApkUninstallEvent(socketClient: SocketClient): Flow<ApkUninstallEvent> = callbackFlow {
        val listener = object : ApkUninstallListener {

            override fun onApkUninstall(apkName: ApkUninstallEvent) {
                trySend(apkName)
            }
        }
        socketClient.registerApkUninstallListener(listener)
        awaitClose {
            socketClient.unregisterApkUninstallListener()
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