package com.core.gscore.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData
import java.net.InetAddress
import java.net.UnknownHostException

class LiveDataNetworkStatus(context: Context) : LiveData<Boolean>() {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val activeNetworks = mutableSetOf<Network>()
    private var lastKnownState: Boolean = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            activeNetworks.remove(network)
            updateNetworkStatus()
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            activeNetworks.add(network)
            checkInternetConnectivityAsync(network)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()
        try {
            connectivityManager.registerNetworkCallback(buildNetworkRequest(), networkCallback)
            updateInitialNetworkStatus()
        } catch (e: Exception) {
            postValue(false)
        }
    }

    override fun onInactive() {
        super.onInactive()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Ignore unregistration errors
        }
    }

    private fun updateInitialNetworkStatus() {
        val isConnected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return postValue(false)
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            capabilities?.hasInternetCapability() == true
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
        postValue(isConnected)
    }

    private fun checkInternetConnectivityAsync(network: Network) {
        Thread {
            val isConnected = try {
                InetAddress.getByName(ROOT_SERVER_CHECK_URL) != null
            } catch (e: UnknownHostException) {
                false
            }

            if (isConnected != lastKnownState) {
                lastKnownState = isConnected
                postValue(isConnected)
            }
        }.start()
    }

    private fun updateNetworkStatus() {
        if (activeNetworks.isEmpty()) {
            postValue(false)
            lastKnownState = false
        }
    }

    private fun buildNetworkRequest(): NetworkRequest {
        return NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
    }

    private fun NetworkCapabilities.hasInternetCapability(): Boolean {
        return hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    companion object {
        private const val ROOT_SERVER_CHECK_URL = "a.root-servers.net"
    }
}