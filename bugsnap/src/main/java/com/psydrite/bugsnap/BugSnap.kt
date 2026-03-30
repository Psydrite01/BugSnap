package com.psydrite.bugsnap


import android.util.Log

object BugSnap {

    private var isInitialized = false

    fun init(apiKey: String) {
        isInitialized = true
        Log.d("BugSnap", "Initialized with key: $apiKey")
    }

    fun log(message: String) {
        checkInit()
        Log.d("BugSnap", message)
    }

    fun captureException(e: Throwable) {
        checkInit()
        Log.e("BugSnap", "Error: ${e.message}")
    }

    private fun checkInit() {
        if (!isInitialized) {
            throw IllegalStateException("BugSnap not initialized")
        }
    }
}