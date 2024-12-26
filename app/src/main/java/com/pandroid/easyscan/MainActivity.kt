package com.pandroid.easyscan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.pandroid.easyscan.ui.scan.BarcodeScanningScreen
import com.pandroid.easyscan.ui.scan.BarcodeScanningViewModel
import com.pandroid.easyscan.ui.theme.EasyScanTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: BarcodeScanningViewModel

    // Create a permission launcher to handle the permission request
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted, show the BarcodeScanningScreen
            setComposeContent()
        } else {
            // Permission is denied, you can handle it accordingly
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[BarcodeScanningViewModel::class.java]

        enableEdgeToEdge()

        // Check if the camera permission is already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, load the UI
            setComposeContent()
        } else {
            // Request camera permission using the ActivityResultLauncher
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Set up the Compose UI
    private fun setComposeContent() {
        setContent {
            EasyScanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    innerPadding

                    BarcodeScanningScreen(viewModel = viewModel)

                }
            }
        }
    }


}

