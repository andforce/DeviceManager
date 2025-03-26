package com.andforce.device.applock

import android.content.Intent

object AppLauncherManager {
    private val controller = AppLockActivityController()

    fun forceStopPackage(pkg: String) = controller.forceStopPackage(pkg)

    fun startListener(action: Action) {
        //controller.setActivityController(action)
    }

    interface Action {
        fun allowLaunch(intent: Intent, pkg: String): Boolean
    }
}