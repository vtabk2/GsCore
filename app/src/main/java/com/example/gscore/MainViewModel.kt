package com.example.gscore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.core.gscore.utils.download.GsDownloadManager
import com.core.gscore.utils.download.GsDownloadManager.DownloadStatus
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val progressLiveData = MutableLiveData<Int>()
    val downloadStatusLiveData = MutableLiveData<DownloadStatus>()

    fun download() {
        val context = getApplication<Application>()
        val folder = context.getExternalFilesDir("download")
        folder?.let {
            if (!it.exists()) {
                it.mkdirs()
            }
        }

        val file = File(folder, "apk")
        if (!file.exists()) {
            file.mkdirs()
        }

        GsDownloadManager.instance.download(
            context = context,
            url = "https://d-03.winudf.com/b/XAPK/Y29tLmhpZ2hzZWN1cmUucGhvdG9mcmFtZV8yMjNfZWJkODczZjY?_fn=UGhvdG8gRnJhbWUgLSBQaG90byBDb2xsYWdlXzUuMy4wX0FQS1B1cmUueGFwaw&_p=Y29tLmhpZ2hzZWN1cmUucGhvdG9mcmFtZQ%3D%3D&download_id=1532602232310734&is_hot=false&k=4aa66749721463730e40d47c48a0876067fe1c76",
            dirPath = file.absolutePath,
            fileName = "Photo Frame - Photo Collage_5.3.0_APKPure.xapk",
            callbackProgress = { progress ->
                progressLiveData.postValue(progress.toInt())
            },
            callbackDownload = { downloadResult ->
                downloadStatusLiveData.postValue(downloadResult.downloadStatus)
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        // viewmodel hủy thì hủy tất cả những thứ đang tải đi
        GsDownloadManager.instance.cancelAll()
    }
}