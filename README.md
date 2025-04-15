# GsCore

**Gradle**
**Step 1.** Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```css
        dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    mavenCentral()
                    maven { url 'https://jitpack.io' }
                }
            }
```
**Step 2.** Add the dependency
```css
        dependencies {
                    implementation 'com.github.vtabk2:GsCore:1.0.4'
            }
```

# Lịch sử cập nhật

**Version 1.0.4**
- Thêm [GsDownloadManager](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/utils/download/GsDownloadManager.kt)
quản lý download có thời gian chờ

```css
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
                    progressLiveData.postValue(progress)
                },
                callbackDownload = { path, downloadStatus: DownloadStatus ->
                }
            )
    }
```

- Đẩy PRDownloader vào GsCore

**Version 1.0.3**

- Sửa lại NetworkUtils
- Thêm check lại ở hàm hasInternetAccessCheck()
- Thêm maxRetries giới hạn số lần thử lại
- Thêm enableDebounce kích hoạt chặn gọi liên tục
- Thêm cancelAllRequests hủy tất cả(khi activity destroy)

```css
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 0..100) {
                Log.d("GsDownloadManager", "MainActivity_onCreate: i = $i")
                NetworkUtils.hasInternetAccessCheck(
                    doTask = {
                        Log.d("GsDownloadManager", "MainActivity_onCreate: SUCCESS")
                    }, doException = { networkError ->
                        Log.d("GsDownloadManager", "MainActivity_onCreate: networkError = " + networkError.name)
                    }, context = this@MainActivity, maxRetries = 3
                )
                delay(500)
            }
        }
```