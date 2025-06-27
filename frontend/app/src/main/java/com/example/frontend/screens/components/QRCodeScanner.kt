package com.example.frontend.screens.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun rememberQRScanner(
    onResult: (String) -> Unit,
    onError: (String) -> Unit = { error ->
        // Default error handling can be overridden
    }
): () -> Unit {
    val context = LocalContext.current
    
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null && result.contents.isNotEmpty()) {
            onResult(result.contents)
        } else {
            onError("Scan cancelled or no data found")
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchScanner(scanLauncher)
        } else {
            onError("Camera permission is required to scan QR codes")
        }
    }
    
    return {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                launchScanner(scanLauncher)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}

@Composable
fun QRCodeScannerButton(
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Scan QR Code",
    onError: (String) -> Unit = { error ->
        // Default error handling can be overridden
    }
) {
    val context = LocalContext.current
    
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null && result.contents.isNotEmpty()) {
            onResult(result.contents)
        } else {
            onError("Scan cancelled or no data found")
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchScanner(scanLauncher)
        } else {
            onError("Camera permission is required to scan QR codes")
        }
    }
    
    Button(
        onClick = {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                    launchScanner(scanLauncher)
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        },
        modifier = modifier
    ) {
        Text(text)
    }
}

private fun launchScanner(scanLauncher: androidx.activity.compose.ManagedActivityResultLauncher<ScanOptions, com.journeyapps.barcodescanner.ScanIntentResult>) {
    val options = ScanOptions()
    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    options.setPrompt("Scan QR Code")
    options.setCameraId(0)
    options.setBeepEnabled(false)
    options.setBarcodeImageEnabled(true)
    options.setOrientationLocked(false)
    options.addExtra("SCAN_MODE", "QR_CODE_MODE")
    options.addExtra("CHARACTER_SET", "UTF-8")
    options.captureActivity = com.journeyapps.barcodescanner.CaptureActivity::class.java
    scanLauncher.launch(options)
}