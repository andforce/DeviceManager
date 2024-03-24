package com.andforce.device.packagemanager.apps

import android.graphics.drawable.Drawable

data class AppBean(val isSystem: Boolean, val packageName: String, val appName: String, val icon: Drawable)