package com.example.gscore

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.core.gscore.utils.download.GsDownloadManager
import com.core.gscore.utils.extensions.invisible
import com.core.gscore.utils.extensions.setClickSafeAll
import com.core.gscore.utils.extensions.visible
import com.core.gscore.utils.network.NetworkUtils
import com.example.gscore.databinding.ActivityMainBinding
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

        GsDownloadManager.instance.register(context = this)

        viewModel.download()
        viewModel.progressLiveData.observe(this) { progress ->
            bindingView.tvProgress.text = String.format(Locale.getDefault(), "%d %%", progress)
        }
        viewModel.downloadStatusLiveData.observe(this) { downloadStatus ->
            when (downloadStatus) {
                GsDownloadManager.DownloadStatus.CONNECTING -> {
                    bindingView.tvRetry.invisible()
                    bindingView.tvProgress.text = "0 %"
                    bindingView.tvResult.text = "CONNECTING"
                }

                GsDownloadManager.DownloadStatus.DOWNLOADING -> {
                    bindingView.tvRetry.invisible()
                    bindingView.tvProgress.visible()
                    bindingView.tvResult.text = "DOWNLOADING"
                }

                GsDownloadManager.DownloadStatus.SUCCESS -> {
                    bindingView.tvRetry.invisible()
                    bindingView.tvProgress.invisible()
                    bindingView.tvResult.text = "SUCCESS"
                }

                GsDownloadManager.DownloadStatus.TIMEOUT -> {
                    bindingView.tvRetry.visible()
                    bindingView.tvProgress.invisible()
                    bindingView.tvResult.text = "TIMEOUT"
                }

                GsDownloadManager.DownloadStatus.CANCEL -> {
                    bindingView.tvRetry.visible()
                    bindingView.tvProgress.invisible()
                    bindingView.tvResult.text = "CANCEL"
                }

                GsDownloadManager.DownloadStatus.SSL_HANDSHAKE -> {
                    bindingView.tvRetry.visible()
                    bindingView.tvProgress.invisible()
                    bindingView.tvResult.text = "SSL_HANDSHAKE"
                }
            }
        }

        bindingView.rivRetry.setClickSafeAll {
            bindingView.tvRetry.invisible()
            bindingView.tvProgress.visible()
            viewModel.download()
        }

        bindingView.tvRetry.setClickSafeAll {
            bindingView.tvRetry.invisible()
            bindingView.tvProgress.visible()
            viewModel.download()
        }

        bindingView.tvCancel.setClickSafeAll {
           viewModel.cancel()
        }
    }

    override fun onDestroy() {
        NetworkUtils.cancelAllRequests()
        super.onDestroy()
    }
}