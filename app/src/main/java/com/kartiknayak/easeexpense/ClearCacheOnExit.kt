package com.kartiknayak.easeexpense

import android.app.Application

class ClearCacheOnExit : Application() {
    override fun onTerminate() {
        super.onTerminate()
        clearCacheDirectory()
    }

    private fun clearCacheDirectory() {
        val cacheDir = cacheDir
        cacheDir?.let { dir ->
            dir.listFiles()?.forEach { file -> file.deleteRecursively() }
        }
    }
}
