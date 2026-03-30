package com.psydrite.bugsnapsdktest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.psydrite.bugsnap.BugSnap
import com.psydrite.bugsnapsdktest.ui.theme.BugSnapSDKTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BugSnapSDKTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BugSnap.init(this, "lofigram-df368")
                    Greeting(
                        name = ", this is a test app for BugSnap SDK",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BugSnapSDKTestTheme {
        Greeting("Android")
    }
}