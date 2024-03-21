package com.andforce.device.manager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andforce.device.applock.AppLauncherManager
import com.andforce.device.manager.databinding.ActivityLockBinding

class LockActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vb = ActivityLockBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        vb.closeApp.setOnClickListener {
            // 推到后台
            //moveTaskToBack(true)
            // finish the activity
//            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
//                addCategory(Intent.CATEGORY_HOME)
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            }
//            startActivity(homeIntent)
            finish()
        }
    }
}