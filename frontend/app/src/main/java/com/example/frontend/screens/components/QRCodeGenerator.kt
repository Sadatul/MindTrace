package com.example.frontend.screens.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder

@Composable
fun QRCode(
    data: String,
    modifier: Modifier = Modifier,
    size: Int = 400
) {
    val context = LocalContext.current
    var qrCodeBitmap by remember(data) { mutableStateOf<Bitmap?>(null) }
    
    LaunchedEffect(data) {
        if (data.isNotBlank()) {
            try {
                val barcodeEncoder = BarcodeEncoder()
                qrCodeBitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)
            } catch (_: WriterException) {
                Toast.makeText(context, "Error generating QR code", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    qrCodeBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "QR Code for: $data",
            modifier = modifier
        )
    }
}