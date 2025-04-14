package com.example.gscore

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.core.gscore.utils.download.GsDownloadManager
import com.core.gscore.utils.network.NetworkUtils
import com.example.gscore.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var bindingView: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bindingView = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingView.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 0..100) {
                Log.d("GsDownloadManager", "MainActivity_onCreate: i = $i")
                NetworkUtils.hasInternetAccessCheck(
                    doTask = {
                        Log.d("GsDownloadManager", "MainActivity_onCreate: SUCCESS")
                    }, doException = { networkError ->
                        Log.d("GsDownloadManager", "MainActivity_onCreate: networkError = " + networkError.name)
                    }, context = this@MainActivity, maxRetries = 3, enableDebounce = false
                )
                delay(500)
            }
        }

        GsDownloadManager.instance.register(context = this)

        viewModel.download()
        viewModel.progressLiveData.observe(this) { progress ->
            bindingView.progress.text = String.format(Locale.getDefault(), "%f %%", progress)
        }
    }

    override fun onDestroy() {
        NetworkUtils.cancelAllRequests()
        super.onDestroy()
    }
}