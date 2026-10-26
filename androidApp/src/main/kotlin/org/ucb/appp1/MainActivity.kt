package org.ucb.appp1

import android.os.Bundle
import android.os.Build
import android.Manifest
import android.content.Intent
import com.google.firebase.FirebaseApp
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    private var destination by mutableStateOf<String?>(null)
    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        destination = routeFromIntent(intent)

        if (Build.VERSION.SDK_INT >= 33 && FirebaseApp.getApps(this).isNotEmpty()) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            FirebaseCourseApp(destination, onDestinationConsumed = { destination = null })
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        destination = routeFromIntent(intent)
    }

    private fun routeFromIntent(intent: Intent?): String? =
        intent?.getStringExtra("route") ?: intent?.data?.lastPathSegment
}

@Preview
@Composable
fun AppAndroidPreview() {
    FirebaseCourseApp()
}
