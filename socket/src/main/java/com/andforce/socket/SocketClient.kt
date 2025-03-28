package com.andforce.socket

import android.util.Log
import com.andforce.socket.apkevent.ApkPushEvent
import com.andforce.socket.apkevent.ApkUninstallEvent
import com.andforce.socket.mouseevent.ApkEventListener
import com.andforce.socket.mouseevent.ApkUninstallListener
import com.andforce.socket.mouseevent.MouseEvent
import com.andforce.socket.mouseevent.MouseEventListener
import com.andforce.socket.mouseevent.MouseMoveEventListener
import com.andforce.socket.mouseevent.SocketStatusListener
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.util.Base64
import java.util.logging.Logger

class SocketClient(private val url: String) {
    private var socket: Socket? = null

    private var mouseEventListener: MouseEventListener? = null
    private var mouseMoveEventListener: MouseMoveEventListener? = null

    private var apkEventListener: ApkEventListener? = null
    private var apkUninstallListener: ApkUninstallListener? = null
    private var socketStatusListener: SocketStatusListener? = null

    fun registerMouseMoveEventListener(listener: MouseMoveEventListener) {
        mouseMoveEventListener = listener
    }

    fun unregisterMouseMoveEventListener() {
        mouseMoveEventListener = null
    }

    fun registerMouseEventListener(listener: MouseEventListener) {
        mouseEventListener = listener
    }
    fun unRegisterMouseEventListener() {
        mouseEventListener = null
    }

    fun registerApkUninstallListener(listener: ApkUninstallListener) {
        apkUninstallListener = listener
    }

    fun unregisterApkUninstallListener() {
        apkUninstallListener = null
    }

    fun registerSocketStatusListener(listener: SocketStatusListener) {
        this.socketStatusListener = listener
    }

    fun unregisterSocketStatusListener() {
        this.socketStatusListener = null
    }
    fun registerApkEventListener(listener: ApkEventListener) {
        apkEventListener = listener
    }

    fun unRegisterApkEventListener() {
        apkEventListener = null
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    fun startConnection() {

        if (isConnected()) {
            return
        }
        socketStatusListener?.onStatus(SocketStatusListener.SocketStatus.CONNECTING)

        Thread {
            while (true) {
                Log.d("SocketClient", "socket status: ${socket?.connected()}")
                Thread.sleep(1000)
            }
        }.start()

        Logger.getAnonymousLogger().level = java.util.logging.Level.ALL
        try {
            socket = IO.socket(url)
        } catch (e: Exception) {
            Log.e("SocketClient", e.toString())
            socketStatusListener?.onStatus(SocketStatusListener.SocketStatus.CONNECT_ERROR)
        }

        socket?.on(Socket.EVENT_CONNECT, Emitter.Listener {
            Log.d("SocketClient", "connect")
            socketStatusListener?.onStatus(SocketStatusListener.SocketStatus.CONNECTED)
        })

        socket?.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            Log.d("SocketClient", "disconnect")
            socketStatusListener?.onStatus(SocketStatusListener.SocketStatus.DISCONNECTED)
        })

        socket?.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener {
            Log.d("SocketClient", "connect error")
            socketStatusListener?.onStatus(SocketStatusListener.SocketStatus.CONNECT_ERROR)
        })

        socket?.on("event", Emitter.Listener { args ->
            Log.d("SocketClient", args[0].toString())
        })

        socket?.on("mouse_event", Emitter.Listener { args ->
            val data = args[0] as JSONObject
            Log.d("SocketClient", "mouse_event: $data")
            val action = data.getString("event")
            when(action) {
                "down" -> {
                    val down = MouseEvent.Down(1, data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
                    mouseEventListener?.onDown(down)
                }
                "up" -> {
                    val down = MouseEvent.Up(2, data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
                    mouseEventListener?.onUp(down)
                }
                "move" -> {
                    val down = MouseEvent.Move(3, data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
                    mouseMoveEventListener?.onMove(down)
                }
            }
        })

//        socket?.on("mouse-down", Emitter.Listener { args ->
//            val data = args[0] as JSONObject
//            val down = MouseEvent.Down(1, data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
//            Log.d("SocketClient", "mousedown" + args[0].toString())
//            mouseEventListener?.onDown(down)
//        })
//
//        socket?.on("mouse-up", Emitter.Listener { args ->
//            Log.d("SocketClient", "mouseup" + args[0].toString())
//            val data = args[0] as JSONObject
//            val down = MouseEvent.Up(2, data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
//            mouseEventListener?.onUp(down)
//        })
//
//        socket?.on("mouse-move", Emitter.Listener { args ->
//            Log.d("SocketClient", "mousemove" + args[0].toString())
//            val data = args[0] as JSONObject
//            val down = MouseEvent.Move(3, data.getInt("x"), data.getInt("y"), data.getInt("width"), data.getInt("height"))
//            mouseMoveEventListener?.onMove(down)
//        })

        socket?.on("apk-upload", Emitter.Listener { args ->
            val data = args[0] as JSONObject
            val down = ApkPushEvent(data.getString("name"), data.getString("path"))
            apkEventListener?.onApk(down)
            Log.d("SocketClient", "apk-upload received: $down, apkEventListener is null? ${apkEventListener == null}")
        })

        socket?.on("uninstall-app", Emitter.Listener { args ->
            Log.d("SocketClient", "uninstall-app" + args[0].toString())
            val data = args[0] as JSONObject
            val down = ApkUninstallEvent(data.getString("name"), data.getString("packageName"))
            apkUninstallListener?.onApkUninstall(down)
        })

        socket?.connect()
    }
    fun send(bitmapArray: ByteArray) {
//        socket?.emit("message", "hello")
        socket?.emit("upload_image", /*Base64.getEncoder().encodeToString(bitmapArray)*/bitmapArray)
    }

    fun release() {

        socket?.off(Socket.EVENT_CONNECT)
        socket?.off(Socket.EVENT_CONNECT_ERROR)
        socket?.off(Socket.EVENT_DISCONNECT)
        socket?.off("event")

        socket?.disconnect()
        socket?.close()
    }
}