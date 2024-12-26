package com.pandroid.easyscan.ui.scan

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.pandroid.easyscan.ui.state.ScanState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BarcodeScanningViewModel : ViewModel() {

    private val _scanState = MutableStateFlow(ScanState())
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    @OptIn(ExperimentalGetImage::class)
    fun scanBarcodes(imageProxy: ImageProxy) {
        if (_scanState.value.isScanning) {
            val image = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
                .build()

            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        _scanState.value = _scanState.value.copy(
                            barcode = barcodes.firstOrNull(),
                            isScanning = false // Stop scanning once a barcode is found
                        )
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close() // Close the imageProxy if scanning is stopped
        }
    }

    fun saveBarcode(barCode: String) {

    }

    fun startScanning() {
        _scanState.value = _scanState.value.copy(isScanning = true, barcode = null) // Clear previous barcode
    }

}
