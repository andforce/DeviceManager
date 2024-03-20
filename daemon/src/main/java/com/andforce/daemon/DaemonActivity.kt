package com.andforce.daemon

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity


// https://blog.csdn.net/chen_md/article/details/128867458
class DaemonActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.daemon_activity)

        val pixelWindow = window.apply {
            setGravity(Gravity.START or Gravity.TOP)
        }
        val attributes = pixelWindow.attributes.apply {
            width = 1
            height = 1
            x = 0
            y = 0
        }
        pixelWindow.attributes = attributes
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}