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
        callbackDownload: ((downloadResult: DownloadResult) -> Unit)? = null,
        timeout: Long = TIMEOUT_DOWNLOADING,
        maxRetries: Int = 3,
        enableDebounce: Boolean = false,
        tag: Any? = null
    ) {
        val path = "$dirPath/$fileName"
        val downloadResult = DownloadResult(
            path = path,
            downloadStatus = DownloadStatus.CONNECTING,
            tag = tag
        )
        callbackDownload?.invoke(downloadResult)

        NetworkUtils.hasInternetAccessCheck(
            doTask = {
                downloadWithTimeout(
                    url = url,
                    dirPath = dirPath,
                    fileName = fileName,
                    downloadResult = downloadResult,
                    callbackProgress = callbackProgress,
                    callbackDownload = callbackDownload,
                    timeout = timeout,
                )
            },
            doException = { networkError ->
                downloadResult.downloadStatus = if (networkError == NetworkUtils.NetworkError.SSL_HANDSHAKE) DownloadStatus.SSL_HANDSHAKE else DownloadStatus.TIMEOUT
                callbackDownload?.invoke(downloadResult)
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
        downloadResult: DownloadResult,
        callbackProgress: ((progress: Float) -> Unit)? = null,
        callbackDownload: ((downloadResult: DownloadResult) -> Unit)? = null,
        timeout: Long = TIMEOUT_DOWNLOADING
    ) {
        val timeoutDownloading = TIMEOUT_DOWNLOADING_MIN.coerceAtLeast(timeout)
        var downloadId = 0

        // tạo thời gian kiểm tra timeout
        val timeoutDownloadingHourglass = object : Hourglass(timeoutDownloading, 1000) {
            override fun onTimerTick(timeRemaining: Long) {
                // Do nothing
            }

            override fun onTimerFinish() {
                if (downloadResult.downloadStatus == DownloadStatus.DOWNLOADING) {
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
                callbackDownload?.invoke(downloadResult.apply {
                    downloadStatus = DownloadStatus.DOWNLOADING
                })
                // hủy đếm thời gian đi
                timeoutDownloadingHourglass.stopTimer()
            }
            .setOnCancelListener {
                callbackDownload?.invoke(downloadResult.apply {
                    downloadStatus = if (downloadResult.downloadStatus == DownloadStatus.DOWNLOADING) {
                        DownloadStatus.CANCEL
                    } else {
                        DownloadStatus.TIMEOUT
                    }
                })
            }
            .setOnProgressListener { progress ->
                callbackProgress?.invoke(progress.currentBytes * 100 / progress.totalBytes.toFloat())
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    callbackDownload?.invoke(downloadResult.apply {
                        downloadStatus = DownloadStatus.SUCCESS
                    })
                }

                override fun onError(error: Error?) {
                    callbackDownload?.invoke(downloadResult.apply {
                        downloadStatus = if (error?.connectionException is SSLHandshakeException) DownloadStatus.SSL_HANDSHAKE else DownloadStatus.TIMEOUT
                    })
                }
            })
    }

    fun cancel(downloadId: Int) {
        PRDownloader.cancel(downloadId)
    }

    fun cancelAll() {
        PRDownloader.cancelAll()
    }

    enum class DownloadStatus {
        /**
         * Đang ở trạng thái kết nối
         */
        CONNECTING,

        /**
         * Đang tải
         */
        DOWNLOADING,

        /**
         * Tải thành công
         */
        SUCCESS,

        /**
         * Quá thời gian chờ
         */
        TIMEOUT,

        /**
         * Hủy tải
         */
        CANCEL,

        /**
         * Lỗi SSL Handshake
         */
        SSL_HANDSHAKE
    }

    class DownloadResult(
        /**
         * Đường dẫn của tệp được tải
         */
        val path: String,
        /**
         * Trạng thái tải
         */
        var downloadStatus: DownloadStatus,
        /**
         * Ghi chú nếu cần
         */
        val tag: Any? = null
    )

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