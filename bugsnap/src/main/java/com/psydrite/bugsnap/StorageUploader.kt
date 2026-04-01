package com.psydrite.bugsnap

import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream

object StorageUploader {

    private val client = OkHttpClient()
    private var _IsInitialized = false
    private var _STORAGE_ID = ""
    private var _PROJECT_ID = ""
    private var _COLLECTIONNAME = ""

    fun init(projectid: String, storageid: String, collectionname: String){
        _PROJECT_ID = projectid
        _STORAGE_ID = storageid
        _COLLECTIONNAME = collectionname

        _IsInitialized = true
    }

    fun uploadScreenshot(
        bitmap: Bitmap,
        onSuccess: (downloadUrl: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Thread {
            try {
                if (!_IsInitialized){
                    return@Thread
                }
                // 1. compress bitmap to JPEG bytes
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val imageBytes = stream.toByteArray()

                // 2. unique filename using timestamp
                val fileName = "$_COLLECTIONNAME/screenshot_${System.currentTimeMillis()}.jpg"

                // 3. upload to Firebase Storage REST API
                val uploadUrl = "https://firebasestorage.googleapis.com/v0/b/$_STORAGE_ID/o/${
                    fileName.replace("/", "%2F")
                }"

                val uploadRequest = Request.Builder()
                    .url(uploadUrl)
                    .post(imageBytes.toRequestBody("image/jpeg".toMediaType()))
                    .build()

                val uploadResponse = client.newCall(uploadRequest).execute()
                val responseBody = uploadResponse.body?.string()

                if (!uploadResponse.isSuccessful || responseBody == null) {
                    onFailure(Exception("Upload failed: ${uploadResponse.code}"))
                    return@Thread
                }

                // 4. parse the download token from response
                val json = JSONObject(responseBody)
                val token = json.getString("downloadTokens")

                // 5. build public download URL
                val downloadUrl = "https://firebasestorage.googleapis.com/v0/b/$_STORAGE_ID/o/${
                    fileName.replace("/", "%2F")
                }?alt=media&token=$token"

                onSuccess(downloadUrl)

            } catch (e: Exception) {
                onFailure(e)
            }
        }.start()
    }
}


internal object BugSnapReporter {
    private var _IsInitialized = false
    private var _PROJECT_ID = ""
    private var _COLLECTIONNAME = ""

    fun init(projectid: String, collectionname: String){
        _PROJECT_ID = projectid
        _COLLECTIONNAME = collectionname

        _IsInitialized = true
    }

    fun sendToFirestore(downloadUrl: String, description: String) {
        val json = """
            {
              "fields": {
                "screenshotUrl": { "stringValue": "$downloadUrl" },
                "description":   { "stringValue": "$description" },
                "timestamp":     { "integerValue": "${System.currentTimeMillis()}" }
              }
            }
        """.trimIndent()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://firestore.googleapis.com/v1/projects/$_PROJECT_ID/databases/(default)/documents/$_COLLECTIONNAME")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        Thread { client.newCall(request).execute() }.start()
    }
}