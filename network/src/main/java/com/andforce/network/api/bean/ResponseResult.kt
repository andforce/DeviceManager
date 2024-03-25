package com.andforce.network.api.bean

import com.google.gson.annotations.SerializedName

data class ResponseResult<T>(
    @SerializedName("code") var code: Int = -1,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: List<T>? = null
) {

    fun isSuccess(): Boolean {
        return code == 0
    }

    override fun toString(): String {
        return "ResponseResult(code=$code, message=$message, data=$data)"
    }
}