# GsCore

Thư viện được tạo ra để chứa các thư viện cần thiết nhất và các phần mở rộng cần thiết nhất của 1 ứng dụng

# Gradle

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
                implementation 'com.github.vtabk2:GsCore:1.0.7'
            }
```

# [AssetManagerExtensions](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/utils/extensions/AssetManagerExtensions.kt)

- Đọc string từ fileName

```css
        val textAsset = context.assets.readTextAsset("fileName")
        val textFromAsset = context.assets.readTextFromAsset("fileName")
```

- Lấy bitmap từ assets

```css
        val bitmapFromAsset = context.assets.getBitmapFromAsset("fileName")
```

# [LiveDataNetworkStatus](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/utils/network/LiveDataNetworkStatus.kt)

- Kiểm tra trạng thái thay đổi kết nối mạng (khi tắt, bật mạng)

```css
        val liveDataNetworkStatus by lazy { LiveDataNetworkStatus(context) }
        
        viewModel.liveDataNetworkStatus.observe(this) { connect ->
            if (connect) {
                reloadDataAfterConnect()
            }
        }
```

# [NetworkUtils](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/utils/network/NetworkUtils.kt)

- Kiểm tra có bật kết nối mạng (bật wifi hoặc dùng mạng điện thoại 3G, 4G, 5G...), không kiểm tra được kết nối có mạng hay không!

```css
       val isInternetAvailable = NetworkUtils.isInternetAvailable(context)
```

> isInternetAvailable = true  -> bật mạng

> isInternetAvailable = false -> tắt mạng

- Kiểm tra kết nối có mạng hay không gồm kiểm tra bật tắt mạng và kiểm tra thử xem có mạng hay không bằng cách ping thử

```css
        NetworkUtils.hasInternetAccessCheck(
            doTask = {
                
            },
            doException = { networkError ->
               
            },
            context = context,
            timeout = DEFAULT_TIMEOUT_MS,
            maxRetries = maxRetries,
            enableDebounce = enableDebounce
        )
```
> maxRetries số lần thử lại (mặc định là 1)

> enableDebounce có kiểm tra liên tiếp không (tránh trường hợp gọi liên tục không có thời giữa các lần gọi)

- Hủy tất cả kiểm tra kết nối

```css
        NetworkUtils.cancelAllRequests()
```

# GsDownloadManager

Được tạo ra để quản lý việc tải dữ liệu có cấu hình thời gian chờ kết nối tải

- Đăng ký cấu hình PRDownloader (thường khởi tạo ở Application)

```css
        GsDownloadManager.instance.register(context = this)
```

- Cấu hình tải dữ liệu

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
```

- Hủy tải theo id

```css
        GsDownloadManager.instance.cancel(downloadId)
```

- Hủy tải tất cả

```css
        GsDownloadManager.instance.cancelAll()
```

# [LifecycleOwnerExtensions](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/utils/extensions/AssetManagerExtensions.kt)

Kiểm tra lifecycleScope hiện tại trạng thái resume không thì mới thực hiện chức năng (tránh trường hợp bị hủy trước khi vào gây ra crash)

```css
        launchWhenResumed {
            // todo
        }
```

# Hourglass 

Hourglass dùng để đếm ngược có tính năng tạm dừng bộ đếm thời gian.

- Khởi tạo

```css
        timerDelay = object : Hourglass(4000, 500) {
            override fun onTimerTick(timeRemaining: Long) {
                // nothing
            }

            override fun onTimerFinish() {
                
            }
        }
```

- Phương pháp để quản lý bộ đếm thời gian

```css
        timerDelay?.startTimer()
        timerDelay?.pauseTimer()
        timerDelay?.resumeTimer()
        timerDelay?.stopTimer()
```

# AspectRatioLayout

Thay đổi kích thước

> widthToHeight dựa vào chiều cao để tính chiều rộng theo

> heightToWidth dựa vào chiều rộng để tính chiều cao theo

```css
        <com.core.gscore.view.AspectRatioLayout xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:app="http://schemas.android.com/apk/res-auto"
            android:id="@+id/arlRoot"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginHorizontal="5dp"
            android:layout_marginVertical="5dp"
            android:background="@drawable/selector_background_item_font"
            app:heightToWidth="0.25">
        </com.core.gscore.view.AspectRatioLayout>
```

- Trong code có thể thay đổi thủ công

```css
        ratioLayout.widthToHeight = width.toFloat() / height
```

# PRDownloader

Tích hợp sẵn thư viện PRDownloader vào sẵn và muốn dùng thì thường dùng qua GsDownloadManager

# Lịch sử cập nhật
**Version 1.0.7**
- Thêm extension getBitmapFromAsset, getTextFromAsset

```css
        context.assets.getTextFromAsset("fileName")
        context.assets.getBitmapFromAsset("fileName")
```

<details>
  <summary>👉 Click để xem lịch sử</summary>

**Version 1.0.6**
- Thêm extensions readTextAsset chuyên để đọc text từ fileName
```css
        context.assets.readTextAsset("fileName")
```

- Sửa lại GsDownloadManager để có thể trả ra downloadId

**Version 1.0.5**
- Thêm [DownloadStatus](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/utils/download/GsDownloadManager.kt) quản lý trạng thái tải
- Đổi [DownloadResult](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/utils/download/GsDownloadManager.kt) giờ sẽ chứa đường dẫn và trạng thái tải
- Sửa lại [GsDownloadManager](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/utils/download/GsDownloadManager.kt) để trạng thái tải về chuẩn hơn

```css
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
```

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
                callbackDownload = { path, downloadResult: DownloadResult ->
                }
            )
    }
```

- Đẩy PRDownloader vào GsCore

**Version 1.0.3**

- Thêm [Hourglass](https://github.com/vtabk2/GsCore/blob/main/GsCore/src/main/java/com/core/gscore/hourglass/Hourglass.java)
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

</details>