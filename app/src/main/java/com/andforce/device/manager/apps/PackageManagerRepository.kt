package com.andforce.device.manager.apps

import android.content.Context
import com.andforce.device.packagemanager.PackageManagerHelper

class PackageManagerRepository {
    fun loadInstalledApps(context: Context): List<AppBean> {
        val list = mutableListOf<AppBean>()

        // 使用协程加载应用列表
        val pm = context.packageManager
        val apps = pm.getInstalledPackages(0)

        for (app in apps) {
            val isSystem = app.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
            val packageName = app.packageName
            val appName = app.applicationInfo.loadLabel(pm).toString()
            val icon = app.applicationInfo.loadIcon(pm)
            list.add(AppBean(isSystem, packageName, appName, icon))
        }
        return list
    }

    fun uninstallApp(applicationContext: Context, it: AppBean) {
        val helper = PackageManagerHelper(applicationContext)
        helper.registerListener(object : PackageManagerHelper.Listener {

            override fun onActionFinished(actionType: Int, success: Boolean) {

            }
        })
        loadInstalledApps(applicationContext)
    }
}