package com.andforce.screen.cast.listener

interface OnImageListener {
    fun onImage(image: ByteArray?)

    fun onFinished()
}