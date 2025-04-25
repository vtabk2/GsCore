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
    private var downloadId: Int = 0

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
            url = "https://d-04.winudf.com/b/XAPK/Y29tLmhpZ2hzZWN1cmUucGhvdG9mcmFtZV8yMjhfYTllY2EwZTM?_fn=UGhvdG8gRnJhbWUgLSBQaG90byBDb2xsYWdlXzUuMy41X0FQS1B1cmUueGFwaw&_p=Y29tLmhpZ2hzZWN1cmUucGhvdG9mcmFtZQ%3D%3D&download_id=1359001696225752&is_hot=false&k=c5066d6130cee63cb7aad5e0f1197cb5680c4643",
            dirPath = file.absolutePath,
            fileName = "Photo Frame - Photo Collage_5.3.5_APKPure.xapk",
            callbackProgress = { progress ->
                progressLiveData.postValue(progress.toInt())
            },
            callbackDownload = { downloadResult ->
                downloadStatusLiveData.postValue(downloadResult.downloadStatus)
            },
            onDownloadId = { downloadId ->
                this.downloadId = downloadId
            },
            timeoutConnect = 15_000
        )
    }

    fun cancel() {
        GsDownloadManager.instance.cancel(downloadId)
    }

    override fun onCleared() {
        super.onCleared()
        // viewmodel hủy thì hủy tất cả những thứ đang tải đi
        GsDownloadManager.instance.cancelAll()
    }
}