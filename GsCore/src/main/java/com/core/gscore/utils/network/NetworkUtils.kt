package com.core.gscore.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.StrictMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory
import javax.net.ssl.SSLHandshakeException

object NetworkUtils {
    private const val DEFAULT_TIMEOUT_MS = 1500
    private const val MAX_CONCURRENT_REQUESTS = 3 // Giới hạn 3 request đồng thời
    private const val DEBOUNCE_TIME_MS = 300L // Chỉ cho phép gọi mỗi 300ms
    private const val GOOGLE_URL = "https://www.google.com"
    private const val DNS_SERVER = "8.8.8.8"
    private const val DNS_PORT = 53

    // Thêm cấu hình retry
    private const val DEFAULT_MAX_RETRIES = 3
    private const val RETRY_INITIAL_DELAY_MS = 500L
    private const val RETRY_MAX_DELAY_MS = 2000L

    // Quản lý Coroutine và Semaphore
    private val netCheckScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val semaphore = Semaphore(MAX_CONCURRENT_REQUESTS)
    private var lastRequestTime = 0L

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        return cm?.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getNetworkCapabilities(activeNetwork)?.run {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                } == true
            } else {
                activeNetworkInfo?.run {
                    isConnected && (type == ConnectivityManager.TYPE_WIFI || type == ConnectivityManager.TYPE_MOBILE)
                } == true
            }
        } == true
    }

    private fun hasInternetAccess(context: Context, timeout: Int = DEFAULT_TIMEOUT_MS): NetworkError {
        if (!isInternetAvailable(context)) return NetworkError.TURN_OFF

        val executor: ExecutorService = Executors.newCachedThreadPool()
        try {
            val task: Callable<Boolean> = Callable {
                StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())

                val connection = URL(GOOGLE_URL).openConnection() as HttpURLConnection
                connection.apply {
                    setRequestProperty("User-Agent", "Android")
                    setRequestProperty("Connection", "close")
                    requestMethod = "GET"
                    connectTimeout = timeout
                    readTimeout = timeout
                    connect()
                }

                if (connection.responseCode == 429) { // Too Many Requests
                    checkDnsConnectivity(timeout)
                } else {
                    connection.responseCode == 200
                }
            }

            val future: Future<Boolean> = executor.submit(task)
            return if (future.get(timeout.toLong(), TimeUnit.MILLISECONDS)) {
                NetworkError.SUCCESS
            } else {
                NetworkError.TIMEOUT
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return when {
                e is ExecutionException && e.cause is SSLHandshakeException -> NetworkError.SSL_HANDSHAKE
                else -> NetworkError.TIMEOUT
            }
        } finally {
            executor.shutdown()
        }
    }

    private fun checkDnsConnectivity(timeout: Int): Boolean {
        return try {
            SocketFactory.getDefault().createSocket()?.use {
                it.connect(InetSocketAddress(DNS_SERVER, DNS_PORT), timeout)
                true
            } == true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // Hàm chính đã tối ưu
    fun hasInternetAccessCheck(
        doTask: () -> Unit,
        doException: (networkError: NetworkError) -> Unit,
        context: Context,
        timeout: Int = DEFAULT_TIMEOUT_MS,
        maxRetries: Int = 1, // Mặc định không retry
        enableDebounce: Boolean = true
    ) {
        val currentTime = System.currentTimeMillis()
        if (enableDebounce && currentTime - lastRequestTime < DEBOUNCE_TIME_MS) return
        lastRequestTime = currentTime

        netCheckScope.launch {
            semaphore.acquire() // Giới hạn đồng thời
            try {
                val networkError = if (maxRetries > 1) {
                    hasInternetAccessWithRetry(context, timeout, maxRetries)
                } else {
                    hasInternetAccess(context, timeout)
                }
                withContext(Dispatchers.Main) {
                    if (networkError == NetworkError.SUCCESS) doTask()
                    else doException(networkError)
                }
            } finally {
                semaphore.release()
            }
        }
    }

    // Hàm kiểm tra mạng với retry (internal)
    private suspend fun hasInternetAccessWithRetry(
        context: Context,
        timeout: Int = DEFAULT_TIMEOUT_MS,
        maxRetries: Int = DEFAULT_MAX_RETRIES,
        initialDelay: Long = RETRY_INITIAL_DELAY_MS,
        maxDelay: Long = RETRY_MAX_DELAY_MS
    ): NetworkError {
        var currentDelay = initialDelay
        var lastError: NetworkError = NetworkError.TIMEOUT

        repeat(maxRetries) {
            when (val result = hasInternetAccess(context, timeout)) {
                NetworkError.SUCCESS -> return result
                NetworkError.TURN_OFF -> return result // Không retry khi mạng tắt
                else -> {
                    lastError = result
                    delay(currentDelay)
                    currentDelay = (currentDelay * 2).coerceAtMost(maxDelay) // Exponential backoff
                }
            }
        }
        return lastError
    }


    // Hủy tất cả khi không cần thiết (ví dụ: Activity destroy)
    fun cancelAllRequests() {
        netCheckScope.cancel()
    }

    enum class NetworkError {
        TURN_OFF,
        TIMEOUT,
        SSL_HANDSHAKE,
        SUCCESS
    }
}
