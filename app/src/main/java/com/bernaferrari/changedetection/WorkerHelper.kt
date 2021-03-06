package com.bernaferrari.changedetection

import android.content.SharedPreferences
import android.os.Build
import androidx.work.*
import com.bernaferrari.changedetection.data.Site
import com.orhanobut.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object WorkerHelper {

    const val UNIQUEWORK = "work"
    const val WIFI = "wifi"
    const val CHARGING = "charging"
    const val BATTERYNOTLOW = "batteryNotLow"
    const val IDLE = "idle"
    const val DELAY = "delay"

    class ConstraintsRequired(
        val wifi: Boolean,
        val charging: Boolean,
        val batteryNotLow: Boolean,
        val deviceIdle: Boolean
    )

    fun fetchFromServer(item: Site): String? {
        val client = OkHttpClient()

        return try {
            val request = Request.Builder()
                .url(item.url)
                .build()

            val response1 = client.newCall(request).execute()
            Logger.d("isSuccessful -> ${response1.isSuccessful}")
            Logger.d("header -> ${response1.headers()}")

            response1.body()?.string() ?: ""
        } catch (e: UnknownHostException) {
            // When internet connection is not available
            Logger.e("UnknownHostException for ${item.url}")
            null
        } catch (e: IllegalArgumentException) {
            // When input is "http://"
            Logger.e("IllegalArgumentException for ${item.url}")
            null
        } catch (e: SocketTimeoutException) {
            // When site is not available
            Logger.e("SocketTimeoutException for ${item.url}")
            ""
        } catch (e: Exception) {
            Logger.e("New Exception for ${item.url}")
            ""
        }
    }

    fun updateWorkerWithConstraints(sharedPrefs: SharedPreferences) {
        val constraints = ConstraintsRequired(
            sharedPrefs.getBoolean(WorkerHelper.WIFI, false),
            sharedPrefs.getBoolean(WorkerHelper.CHARGING, false),
            sharedPrefs.getBoolean(WorkerHelper.BATTERYNOTLOW, false),
            sharedPrefs.getBoolean(WorkerHelper.IDLE, false)
        )

        cancelWork()
        if (sharedPrefs.getBoolean("backgroundSync", false)) {
            reloadWorkManager(sharedPrefs.getLong(WorkerHelper.DELAY, 30), constraints)
        }
    }

    private fun reloadWorkManager(delay: Long = 15, constraints: ConstraintsRequired) {
        cancelWork()

        val workerConstraints = Constraints.Builder()
        workerConstraints.setRequiredNetworkType(NetworkType.CONNECTED)

        val inputData = Data.Builder()
            .putBoolean(WIFI, constraints.wifi)
            .build()

        if (constraints.batteryNotLow) workerConstraints.setRequiresBatteryNotLow(true)
        if (constraints.charging) workerConstraints.setRequiresCharging(true)
        if (Build.VERSION.SDK_INT >= 23 && constraints.deviceIdle) workerConstraints.setRequiresDeviceIdle(
            true
        )

        val photoWork = OneTimeWorkRequest.Builder(
            SyncWorker::class.java
        )
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .setConstraints(workerConstraints.build())
            .setInputData(inputData)
            .build()

        WorkManager.getInstance()
            .beginUniqueWork(WorkerHelper.UNIQUEWORK, ExistingWorkPolicy.REPLACE, photoWork)
            .enqueue()
    }

    fun cancelWork() {
        WorkManager.getInstance().cancelUniqueWork(WorkerHelper.UNIQUEWORK)
    }
}