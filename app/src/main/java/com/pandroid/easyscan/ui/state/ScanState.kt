package com.pandroid.easyscan.ui.state

import com.google.mlkit.vision.barcode.common.Barcode

data class ScanState(
    val barcode: Barcode? = null,
    val isScanning: Boolean = false
)