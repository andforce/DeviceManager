package cn.llnao.mdm.vpn

import android.content.Intent
import android.net.VpnService

class MyVpnService : VpnService() {

    override fun onCreate() {
        super.onCreate()

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onRevoke() {
        super.onRevoke()
    }
    override fun onDestroy() {
        super.onDestroy()
    }

}