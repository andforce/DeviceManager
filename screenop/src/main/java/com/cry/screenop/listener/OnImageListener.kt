package com.cry.screenop.listener

import android.media.Image

interface OnImageListener {
    fun onImage(image: Image)

    fun onFinished()
}