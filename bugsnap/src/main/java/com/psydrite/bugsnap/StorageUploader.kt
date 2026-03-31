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
    private const val BUCKET = "lofigram-df368.firebasestorage.app"
    private const val PROJECT_ID = "lofigram-df368"

    fun uploadScreenshot(
        bitmap: Bitmap,
        onSuccess: (downloadUrl: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Thread {
            try {
                // 1. compress bitmap to JPEG bytes
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val imageBytes = stream.toByteArray()

                // 2. unique filename using timestamp
                val fileName = "bugsnap/screenshot_${System.currentTimeMillis()}.jpg"

                // 3. upload to Firebase Storage REST API
                val uploadUrl = "https://firebasestorage.googleapis.com/v0/b/$BUCKET/o/${
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
                val downloadUrl = "https://firebasestorage.googleapis.com/v0/b/$BUCKET/o/${
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
            .url("https://firestore.googleapis.com/v1/projects/lofigram-df368/databases/(default)/documents/BugReports")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        Thread { client.newCall(request).execute() }.start()
    }
}