package com.core.gscore.utils.network

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.StrictMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = this.isConnected
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    private fun hasInternetAccess(context: Context, timeout: Int = 1500): NetworkError {
        return if (isInternetAvailable(context)) {
            try {
                val executor: ExecutorService = Executors.newCachedThreadPool()
                val task: Callable<Boolean> = Callable<Boolean> {
                    val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(policy)

                    val httpURLConnection: HttpURLConnection = URL("https://www.google.com").openConnection() as HttpURLConnection
                    httpURLConnection.setRequestProperty("User-Agent", "Android")
                    httpURLConnection.setRequestProperty("Connection", "close")
                    httpURLConnection.requestMethod = "GET"
                    httpURLConnection.connectTimeout = timeout
                    httpURLConnection.readTimeout = timeout
                    httpURLConnection.connect()

                    if (httpURLConnection.responseCode == 429) {// Too Many Requests
                        try {
                            SocketFactory.getDefault().createSocket()?.use { it.connect(InetSocketAddress("8.8.8.8", 53), timeout) } ?: false
                            true
                        } catch (e: IOException) {
                            false
                        }
                    } else {
                        httpURLConnection.responseCode == 200
                    }
                }
                val future: Future<Boolean> = executor.submit(task)
                val success = future.get(timeout.toLong(), TimeUnit.MILLISECONDS)
                if (success) {
                    NetworkError.SUCCESS
                } else {
                    NetworkError.TIMEOUT
                }
            } catch (e: Exception) {
                e.printStackTrace()
                when (e) {
                    is ExecutionException -> {
                        if (e.cause is SSLHandshakeException) {
                            NetworkError.SSL_HANDSHAKE
                        } else {
                            NetworkError.TIMEOUT
                        }
                    }

                    else -> {
                        NetworkError.TIMEOUT
                    }
                }
            }
        } else {
            NetworkError.TURN_OFF
        }
    }

    fun hasInternetAccessCheck(doTask: () -> Unit, doException: (networkError: NetworkError) -> Unit, activity: Activity, timeout: Int = 1500) {
        hasInternetAccessCheck(doTask = doTask, doException = doException, context = activity, timeout = timeout)
    }

    fun hasInternetAccessCheck(doTask: () -> Unit, doException: (networkError: NetworkError) -> Unit, context: Context, timeout: Int = 1500) {
        CoroutineScope(Dispatchers.IO).launch {
            val networkError = hasInternetAccess(context, timeout)
            val success = when (networkError) {
                NetworkError.SUCCESS -> {
                    true
                }

                NetworkError.TIMEOUT -> {
                    false
                }

                NetworkError.SSL_HANDSHAKE -> {
                    false
                }

                NetworkError.TURN_OFF -> {
                    false
                }
            }
            withContext(Dispatchers.Main) {
                if (success) {
                    doTask.invoke()
                } else {
                    doException.invoke(networkError)
                }
            }
        }
    }

    enum class NetworkError {
        TURN_OFF, TIMEOUT, SSL_HANDSHAKE, SUCCESS
    }
}
