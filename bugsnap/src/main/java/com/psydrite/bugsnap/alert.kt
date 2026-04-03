package com.psydrite.bugsnap

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// these are the internal state — only SDK touches these
internal var bugSnapBitmap by mutableStateOf<Bitmap?>(null)
internal var bugSnapVisible by mutableStateOf(false)

@Composable
fun BugSnapOverlay() {
    var description by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var finishMessage by remember { mutableStateOf("") }

    if (bugSnapVisible && bugSnapBitmap != null) {
        AlertDialog(
            onDismissRequest = {
                bugSnapVisible = false
                bugSnapBitmap = null
            },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Report a Bug",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // screenshot preview
                    bugSnapBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Screenshot",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        )
                    }

                    // description field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Describe the bug") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            isUploading = true
                            bugSnapBitmap?.let { bmp ->
                                StorageUploader.uploadScreenshot(
                                    bitmap = bmp,
                                    onSuccess = { downloadUrl ->
                                        finishMessage = "Uploaded Successfully"
                                        BugSnapReporter.sendToFirestore(downloadUrl, description)
                                        bugSnapVisible = false
                                        bugSnapBitmap = null
                                        isUploading = false
                                        description = ""
                                    },
                                    onFailure = {
                                        finishMessage = "Upload failed, try again"
                                        isUploading = false
                                    }
                                )
                            }
                        },
                        enabled = !isUploading,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.primary
                                        )
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else if (finishMessage.isNotEmpty()) {
                                Text(
                                    finishMessage,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            else{
                                Text(
                                    "Send Report",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            bugSnapVisible = false
                            bugSnapBitmap = null
                            description = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}