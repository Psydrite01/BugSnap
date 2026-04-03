package com.psydrite.bugsnap


import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.lang.ref.WeakReference

object BugSnap {

    private var _isInitialized = false
    private var _API_KEY = ""
    private var _projectKey = ""
    private var _collectionName = "BugSnap"
    private var shakeDetector: ShakeDetector? = null
    private var activityRef: WeakReference<Activity>? = null

    fun init(
        activity: Activity,
        projectKey: String,
        FBstorageUrl: String,
        ApiKey: String = "",
        collectionName: String = _collectionName
    ) {
        if (_isInitialized){
            return
        }
        _projectKey = projectKey
        _collectionName = collectionName
        _API_KEY = ApiKey
        _isInitialized = true

        StorageUploader.init(projectKey, FBstorageUrl, _API_KEY,_collectionName)
        BugSnapReporter.init(projectKey, _API_KEY, _collectionName)

        activityRef = WeakReference(activity)
        shakeDetector = ShakeDetector(activity) {
            captureAndSend()
        }
        shakeDetector?.start()

        Log.d("BugSnap", "Initialized with key: $projectKey")
    }

    fun stop(){
        shakeDetector?.stop()
        shakeDetector = null
        activityRef?.clear()
        activityRef = null
        _isInitialized = false
    }

    private fun captureAndSend() {
        val activity = activityRef?.get()
        if (activity == null || activity.isFinishing){
            return
        }

        ScreenshotCapture.capture(activity) { bitmap ->
            if (bitmap != null) {
                bugSnapBitmap = bitmap   // ← set state
                bugSnapVisible = true    // ← dialog reacts automatically
            }
        }
    }
}