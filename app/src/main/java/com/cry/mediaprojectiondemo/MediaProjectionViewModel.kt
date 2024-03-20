package com.cry.mediaprojectiondemo

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MediaProjectionViewModel(act: AppCompatActivity) : ViewModel() {

    private val _result = MutableLiveData<Result>()
    val result: LiveData<Result> get() = _result

    private var mpm: MediaProjectionManager? = null

    init {
        mpm = act.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var launcher = act.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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

    fun createScreenCaptureIntent() {
        viewModelScope.launch {
            mpm?.createScreenCaptureIntent()?.let {
                launcher.launch(it)
            }
        }
    }


    sealed class Result {
        data class Success(val data: Intent, val resultCode: Int) : Result()
        data object PermissionDenied : Result()
    }
}