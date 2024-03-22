package com.andforce.device.manager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andforce.device.manager.databinding.ActivityLockBinding

class LockActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vb = ActivityLockBinding.inflate(layoutInflater).also {
            setContentView(it.root)
        }

        vb.closeApp.setOnClickListener {
            finish()
        }
    }
}