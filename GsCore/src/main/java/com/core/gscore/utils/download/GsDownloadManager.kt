package com.core.gscore.utils.download

import android.annotation.SuppressLint
import android.content.Context
import com.core.gscore.hourglass.Hourglass
import com.core.gscore.utils.network.NetworkUtils
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import javax.net.ssl.SSLHandshakeException

class GsDownloadManager() {

    fun register(
        context: Context,
        config: PRDownloaderConfig = PRDownloaderConfig.newBuilder().build()
    ) {
        PRDownloader.initialize(context, config)
    }

    fun download(
        context: Context,
        url: String,
        dirPath: String,
        fileName: String,
        callbackProgress: ((progress: Float) -> Unit)? = null,
        callbackDownload: ((path: String, downloadStatus: DownloadStatus) -> Unit)? = null,
        timeout: Long = TIMEOUT_DOWNLOADING,
        maxRetries: Int = 3,
        enableDebounce: Boolean = false,
        tag: Any? = null
    ) {
        val path = "$dirPath/$fileName"
        callbackDownload?.invoke(path, DownloadStatus.CONNECTING)
        NetworkUtils.hasInternetAccessCheck(
            doTask = {
                downloadWithTimeout(
                    url = url,
                    dirPath = dirPath,
                    fileName = fileName,
                    callbackProgress = callbackProgress,
                    callbackDownload = callbackDownload,
                    timeout = timeout,
                    tag = tag
                )
            },
            doException = { networkError ->
                callbackDownload?.invoke(path, if (networkError == NetworkUtils.NetworkError.SSL_HANDSHAKE) DownloadStatus.SSL_HANDSHAKE else DownloadStatus.TIMEOUT)
            },
            context = context,
            maxRetries = maxRetries,
            enableDebounce = enableDebounce
        )
    }

    private fun downloadWithTimeout(
        url: String,
        dirPath: String,
        fileName: String,
        callbackProgress: ((progress: Float) -> Unit)? = null,
        callbackDownload: ((path: String, downloadStatus: DownloadStatus) -> Unit)? = null,
        timeout: Long = TIMEOUT_DOWNLOADING,
        tag: Any? = null
    ): Int {
        val path = "$dirPath/$fileName"
        val timeoutDownloading = TIMEOUT_DOWNLOADING_MIN.coerceAtLeast(timeout)
        var isStartDownload = false
        var downloadId = 0

        // tạo thời gian kiểm tra timeout
        val timeoutDownloadingHourglass = object : Hourglass(timeoutDownloading, 1000) {
            override fun onTimerTick(timeRemaining: Long) {
                // Do nothing
            }

            override fun onTimerFinish() {
                if (isStartDownload) {
                    return
                }
                cancel(downloadId)
            }
        }
        timeoutDownloadingHourglass.startTimer()

        // bắt đầu tải
        downloadId = PRDownloader
            .download(url, dirPath, fileName)
            .build()
            .setOnStartOrResumeListener {
                isStartDownload = true
                callbackDownload?.invoke(path, DownloadStatus.DOWNLOADING)
                // hủy đếm thời gian đi
                timeoutDownloadingHourglass.stopTimer()
            }
            .setOnCancelListener {
                callbackDownload?.invoke(path, DownloadStatus.CANCEL)
            }
            .setOnProgressListener { progress ->
                callbackProgress?.invoke(progress.currentBytes * 100 / progress.totalBytes.toFloat())
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    callbackDownload?.invoke(path, DownloadStatus.SUCCESS)
                }

                override fun onError(error: Error?) {
                    callbackDownload?.invoke(path, if (error?.connectionException is SSLHandshakeException) DownloadStatus.SSL_HANDSHAKE else DownloadStatus.TIMEOUT)
                }
            })
        return downloadId
    }

    fun cancel(downloadId: Int) {
        PRDownloader.cancel(downloadId)
    }

    fun cancelAll() {
        PRDownloader.cancelAll()
    }

    enum class DownloadStatus {
        CONNECTING,
        DOWNLOADING,
        SUCCESS,
        TIMEOUT,
        CANCEL,
        SSL_HANDSHAKE
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var singleton: GsDownloadManager? = null

        const val TIMEOUT_DOWNLOADING = 30_000L
        const val TIMEOUT_DOWNLOADING_MIN = 15_000L

        /***
         * returns an instance of this class. if singleton is null create an instance
         * else return  the current instance
         * @return
         */
        val instance: GsDownloadManager
            get() {
                if (singleton == null) {
                    singleton = GsDownloadManager()
                }
                return singleton as GsDownloadManager
            }
    }
}