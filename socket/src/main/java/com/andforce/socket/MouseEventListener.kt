package com.andforce.socket

interface MouseEventListener {

    fun onDown(mouseEvent: MouseEvent)
    fun onMove(mouseEvent: MouseEvent)
    fun onUp(mouseEvent: MouseEvent)
}

interface ApkEventListener {
    fun onApk(apkName: ApkEvent)
}