package com.andforce.screen.cast.listener

import android.media.Image

interface OnImageListener {
    fun onImage(image: Image)

    fun onFinished()
}