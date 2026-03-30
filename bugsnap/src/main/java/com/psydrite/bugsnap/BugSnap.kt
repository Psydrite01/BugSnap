package com.psydrite.bugsnap


import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object BugSnap {

    private var isInitialized = false

    fun init(apiKey: String) {
        isInitialized = true
        Log.d("BugSnap", "Initialized with key: $apiKey")

        sendData()
    }

    fun sendData() {
        val url = "https://firestore.googleapis.com/v1/projects/task-manger-database/databases/(default)/documents/BugSnap"

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
        if (!isInitialized) {
            throw IllegalStateException("BugSnap not initialized")
        }
    }
}