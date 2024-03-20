package com.example.plugindemo.installapk

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.andforce.device.manager.R

class InstallApkDemoActivity : AppCompatActivity() {
    private var mPackageManagerHelper: PackageManagerHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.install_apk_session_api)
        mPackageManagerHelper = PackageManagerHelper(this@InstallApkDemoActivity).also {
            it.registerListener { actionType, success ->
                Log.d(
                    TAG,
                    "onActionFinished: $actionType $success"
                )
            }
        }

        findViewById<View>(R.id.install_sync).setOnClickListener {
            mPackageManagerHelper?.installFromAssert("notes.apk")
        }
        findViewById<View>(R.id.uninstall_sync).setOnClickListener {
            mPackageManagerHelper?.deletePackage("com.smartisan.notes")
        }
        findViewById<View>(R.id.execute_cmd).setOnClickListener {
            CmdManager.executeShellCommand("pm list package").also {
                Log.d(TAG, "executeShellCommand: $it")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mPackageManagerHelper?.unregisterListener()
    }

    companion object {
        private const val TAG = "InstallApkDemoActivity"
    }
}