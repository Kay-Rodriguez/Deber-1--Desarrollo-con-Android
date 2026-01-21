package com.epn.miapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.FileProvider
import com.epn.miapp.ui.theme.MiappTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiappTheme {
                val context = this
                val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
                var imageUri: Uri? = null

                val takePictureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicture(),
                    onResult = { success ->
                        if (success) {
                            val resultIntent = Intent().apply {
                                data = imageUri
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                        } else {
                            setResult(Activity.RESULT_CANCELED)
                        }
                        finish()
                    }
                )

                LaunchedEffect(Unit) {
                    if (cameraPermissionState.status.isGranted) {
                        imageUri = createImageFile()
                        takePictureLauncher.launch(imageUri)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
            }
        }
    }

    private fun createImageFile(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            imageFile
        )
    }
}
