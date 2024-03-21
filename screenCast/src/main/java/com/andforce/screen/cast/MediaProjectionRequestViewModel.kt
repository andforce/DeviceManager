package com.andforce.screen.cast

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MediaProjectionRequestViewModel(act: AppCompatActivity) : ViewModel() {

    private val _result = MutableLiveData<Result>()
    val permissionResult: LiveData<Result> get() = _result

    private val mpm: MediaProjectionManager by lazy {
        act.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var activityResultLauncher = act.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        when (it.resultCode) {
            Activity.RESULT_OK -> {
                it.data?.let { data ->
                    _result.value = Result.Success(data, it.resultCode)
                } ?: run {
                    _result.value = Result.PermissionDenied
                }
            }

            else -> {
                _result.value = Result.PermissionDenied
            }
        }
    }

    fun requestScreenCapturePermission() {
        activityResultLauncher.launch(mpm.createScreenCaptureIntent())
    }


    sealed class Result {
        data class Success(val data: Intent, val resultCode: Int) : Result()
        data object PermissionDenied : Result()
    }
}