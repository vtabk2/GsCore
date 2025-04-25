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

    /**
     * Đăng ký cấu hình PRDownloader
     */
    fun register(
        context: Context,
        config: PRDownloaderConfig = PRDownloaderConfig.newBuilder().build()
    ) {
        PRDownloader.initialize(context, config)
    }

    /**
     * @param url địa chỉ tải tệp
     * @param dirPath thư mục lưu tệp
     * @param fileName tên của tệp
     * @param callbackProgress chạy % khi tải
     * @param callbackDownload trả về trạng thái tải
     * @param onDownloadId trả về downloadId để dùng hủy tải dữ liệu khi cần
     * @param timeoutConnect thời gian chờ kết nối để tải tính bằng milliseconds
     * @param maxRetries số lần thử lại tối đa khi kiểm tra kết nối mạng
     * @param enableDebounce có kiểm tra chặn kiểm tra mạng liên tục không
     * @param tag các chú thích kèm theo của bạn nếu muốn
     */
    fun download(
        context: Context,
        url: String,
        dirPath: String,
        fileName: String,
        callbackProgress: ((progress: Float) -> Unit)? = null,
        callbackDownload: ((downloadResult: DownloadResult) -> Unit)? = null,
        onDownloadId: (Int) -> Unit,
        timeoutConnect: Long = TIMEOUT_CONNECT_DOWNLOADING_DEFAULT,
        maxRetries: Int = 3,
        enableDebounce: Boolean = false,
        tag: Any? = null
    ) {
        val path = "$dirPath/$fileName"

        // Khởi tạo trạng thái kết nối
        val downloadResult = DownloadResult(
            path = path,
            downloadStatus = DownloadStatus.CONNECTING,
            tag = tag
        )
        callbackDownload?.invoke(downloadResult)

        NetworkUtils.hasInternetAccessCheck(
            doTask = {
                val downloadId = downloadWithTimeout(
                    url = url,
                    dirPath = dirPath,
                    fileName = fileName,
                    downloadResult = downloadResult,
                    callbackProgress = callbackProgress,
                    callbackDownload = callbackDownload,
                    timeoutConnect = timeoutConnect,
                )
                onDownloadId(downloadId)
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

    /**
     * @param url địa chỉ tải tệp
     * @param dirPath thư mục lưu tệp
     * @param fileName tên của tệp
     * @param downloadResult trạng thái tải
     * @param callbackProgress chạy % khi tải
     * @param callbackDownload trả về trạng thái tải
     * @param timeoutConnect thời gian chờ kết nối để tải
     */
    private fun downloadWithTimeout(
        url: String,
        dirPath: String,
        fileName: String,
        downloadResult: DownloadResult,
        callbackProgress: ((progress: Float) -> Unit)? = null,
        callbackDownload: ((downloadResult: DownloadResult) -> Unit)? = null,
        timeoutConnect: Long
    ): Int {
        val timeoutDownloading = TIMEOUT_CONNECT_DOWNLOADING_MIN.coerceAtLeast(timeoutConnect)
        var downloadId = 0

        // tạo thời gian kiểm tra timeout
        val timeoutDownloadingHourglass = object : Hourglass(timeoutDownloading, 1000) {
            override fun onTimerTick(timeRemaining: Long) {
                // Do nothing
            }

            override fun onTimerFinish() {
                // Hết thời gian chờ mà vẫn trạng thái đang kết nối thì sẽ hủy tải đi
                if (downloadResult.downloadStatus == DownloadStatus.CONNECTING) {
                    cancel(downloadId)
                }
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
                    when (downloadResult.downloadStatus) {
                        /**
                         * Thường là trường hợp hủy do hết thời gian chờ mà vẫn chưa kết nối được
                         */
                        DownloadStatus.CONNECTING -> downloadStatus = DownloadStatus.TIMEOUT
                        /**
                         * Thường là truờng hợp hủy chủ động khi đã bắt đầu tải rồi
                         */
                        DownloadStatus.DOWNLOADING -> downloadStatus = DownloadStatus.CANCEL
                        else -> {

                        }
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
        return downloadId
    }

    /**
     * Hủy tải theo id
     */
    fun cancel(downloadId: Int) {
        PRDownloader.cancel(downloadId)
    }

    /**
     * Hủy tải tất cả
     */
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

        const val TIMEOUT_CONNECT_DOWNLOADING_DEFAULT = 30_000L
        const val TIMEOUT_CONNECT_DOWNLOADING_MIN = 15_000L

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