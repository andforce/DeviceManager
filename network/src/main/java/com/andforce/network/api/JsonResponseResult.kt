package com.andforce.network.api

import com.google.gson.annotations.SerializedName

data class JsonResponseResult<T>(
    @SerializedName("code") var code: Int = -1,
    @SerializedName("message") var message: String? = "",
    @SerializedName("data") var data: List<T>? = null
) {

    override fun toString(): String {
        return "ResponseResult(code=$code, message=$message, data=$data)"
    }
}