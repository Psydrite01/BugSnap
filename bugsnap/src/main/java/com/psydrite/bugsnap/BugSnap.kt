package com.psydrite.bugsnap


import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object BugSnap {

    private var _isInitialized = false
    private var _projectKey = ""
    private var _collectionName = "BugSnap"
    private var shakeDetector: ShakeDetector? = null

    fun init(context: Context, projectKey: String, collectionName: String = _collectionName) {
        _projectKey = projectKey
        _collectionName = collectionName
        _isInitialized = true

        shakeDetector = ShakeDetector(context) {
            // called when shake is detected
            Toast.makeText(context, "Shake detected", Toast.LENGTH_SHORT).show()
            // later: captureScreenshot() + sendData()
        }
        shakeDetector?.start()

        Log.d("BugSnap", "Initialized with key: $projectKey")
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