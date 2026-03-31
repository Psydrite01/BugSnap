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
    private var _projectKey = ""
    private var _collectionName = "BugSnap"
    private var shakeDetector: ShakeDetector? = null
    private var activityRef: WeakReference<Activity>? = null

    fun init(activity: Activity, projectKey: String, collectionName: String = _collectionName) {
        _projectKey = projectKey
        _collectionName = collectionName
        _isInitialized = true

        activityRef = WeakReference(activity)
        shakeDetector = ShakeDetector(activity) {
//            Toast.makeText(context, "Shake detected", Toast.LENGTH_SHORT).show()
            Log.d("BugSnap", "shake is detected")
            captureAndSend()
        }
        shakeDetector?.start()

        Log.d("BugSnap", "Initialized with key: $projectKey")
    }

    private fun captureAndSend() {
        val activity = activityRef?.get()
        if (activity == null || activity.isFinishing){
//            Toast.makeText(context, "activity is null, or finishing", Toast.LENGTH_SHORT).show()
            return
        }

        ScreenshotCapture.capture(activity) { bitmap ->
            if (bitmap != null) {
//                Toast.makeText(context, "Screenshot captured, preparing to send", Toast.LENGTH_SHORT).show()
                bugSnapBitmap = bitmap   // ← set state
                bugSnapVisible = true    // ← dialog reacts automatically
            }
//            Toast.makeText(context, "bitmap is null", Toast.LENGTH_SHORT).show()
        }
    }

    fun stop() {
        shakeDetector?.stop()
    }

    fun sendData(context: Context) {
        if (!_isInitialized){
            Toast.makeText(context, "BugSnap is not initialized!", Toast.LENGTH_SHORT).show()
            return
        }
        val url = "https://firestore.googleapis.com/v1/projects/task-manger-database/databases/(default)/documents/${BugSnap}"

        val json = """
        {
          "fields": {
            "name": { "stringValue": "John" },
            "age": { "integerValue": "25" },
            "timestamp": { "integerValue": "${System.currentTimeMillis()}" }
          }
        }
    """.trimIndent()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody("application/json".toMediaType()))  // POST not PUT
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                Log.d("BugSnap", "Response: ${response.code} ${response.body?.string()}")
            } catch (e: Exception) {
                Log.e("BugSnap", "Error: ${e.message}")
            }
        }.start()  // run on background thread, never on main thread
    }

    private fun checkInit() {
        if (!_isInitialized) {
            throw IllegalStateException("BugSnap not initialized")
        }
    }
}