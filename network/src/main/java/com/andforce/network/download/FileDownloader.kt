package com.andforce.network.download

import android.content.Context

object FileDownloader {

    suspend fun download(
        context: Context,
        url: String,
        action: DownloadBuilder.() -> Unit
    ) {
        val build = DownloadBuilder(context, url)
        build.action()
        build.startDownload()
    }
}