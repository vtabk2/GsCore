package com.example.gscore

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.core.gscore.utils.download.GsDownloadManager
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
    }

    override fun onDestroy() {
        NetworkUtils.cancelAllRequests()
        super.onDestroy()
    }
}