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
                    implementation 'com.github.vtabk2:GsCore:1.0.3'
            }
```

# Lịch sử cập nhật

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