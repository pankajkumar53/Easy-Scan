package com.pandroid.easyscan.ui.scan

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.common.util.concurrent.ListenableFuture
import com.pandroid.easyscan.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun BarcodeScanningScreen(viewModel: BarcodeScanningViewModel) {

    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()

    LaunchedEffect(Unit) { viewModel.startScanning() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (scanState.isScanning) {
            CameraPreview(viewModel)
        }

        scanState.barcode?.let { barcode ->
            barcode.rawValue?.let {
                ScannedBarcodeDisplay(viewModel, it) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(barcode.rawValue))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "$e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedButton(onClick = { viewModel.startScanning() }) {
                Text("Rescan", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/*@Composable
fun CameraPreview(viewModel: BarcodeScanningViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }
    val cameraInstance = remember { mutableStateOf<Camera?>(null) }
    val isFlashOn = remember { mutableStateOf(false) }
    val ambientLightLevel = remember { mutableFloatStateOf(0f) }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    SetupLightSensor(sensorManager, ambientLightLevel)


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CameraView(previewView, cameraInstance)

        ScanningOverlay(animationRes = R.raw.animation)

        SetupCamera(
            cameraProviderFuture,
            lifecycleOwner,
            previewView,
            cameraExecutor,
            cameraInstance,
            ambientLightLevel,
            isFlashOn,
            viewModel
        )
    }
}*/
@Composable
fun CameraPreview(viewModel: BarcodeScanningViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }
    val cameraInstance = remember { mutableStateOf<Camera?>(null) }
    val isFlashOn = remember { mutableStateOf(false) }
    val ambientLightLevel = remember { mutableFloatStateOf(0f) }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    SetupLightSensor(sensorManager, ambientLightLevel)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)) // Add translucent black background
    ) {
        CameraView(previewView, cameraInstance)

        ScanningOverlay(animationRes = R.raw.animation)

        SetupCamera(
            cameraProviderFuture,
            lifecycleOwner,
            previewView,
            cameraExecutor,
            cameraInstance,
            ambientLightLevel,
            isFlashOn,
            viewModel
        )
    }
}

@Composable
fun ScanningOverlay(animationRes: Int) {
    // Lottie animation state
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Card(
        modifier = Modifier
            .padding(70.dp)
            .size(240.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No elevation
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}


@Composable
fun ScannedBarcodeDisplay(
    viewModel: BarcodeScanningViewModel,
    barcodeValue: String,
    onClick: () -> Unit
) {
    Text(
        text = "Barcode: $barcodeValue",
        modifier = Modifier
            .clickable { onClick() }
            .padding(16.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp
    )
    Button(onClick = {
        viewModel.saveBarcode(barcodeValue)
    }) {
        Text(text = "Save data")
    }
}

@Composable
fun CameraView(previewView: PreviewView, cameraInstance: MutableState<Camera?>) {
    AndroidView(
        factory = { previewView },
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                val factory = previewView.meteringPointFactory
                val meteringPoint =
                    factory.createPoint(previewView.width / 2f, previewView.height / 2f)
                cameraInstance.value?.cameraControl?.startFocusAndMetering(
                    FocusMeteringAction
                        .Builder(meteringPoint)
                        .build()
                )
            }
    )
}

/*@Composable
fun ScanningOverlay(animationRes: Int) {

    // Lottie animation state
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(animationRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Card(
        modifier = Modifier
            .padding(70.dp)
            .size(240.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxSize()
        )
    }

}*/

@Composable
fun SetupLightSensor(
    sensorManager: SensorManager,
    ambientLightLevel: MutableState<Float>
) {
    DisposableEffect(Unit) {
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let { ambientLightLevel.value = it.values[0] }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(
            lightSensorListener,
            lightSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        onDispose { sensorManager.unregisterListener(lightSensorListener) }
    }
}


@Composable
fun SetupCamera(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraExecutor: ExecutorService,
    cameraInstance: MutableState<Camera?>,
    ambientLightLevel: MutableState<Float>,
    isFlashOn: MutableState<Boolean>,
    viewModel: BarcodeScanningViewModel
) {
    LaunchedEffect(Unit) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        handleFrameProcessing(
                            imageProxy,
                            ambientLightLevel,
                            isFlashOn,
                            cameraInstance.value,
                            viewModel
                        )
                    }
                }
            try {
                cameraProvider.unbindAll()
                cameraInstance.value = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(previewView.context))
    }
}

private fun handleFrameProcessing(
    imageProxy: ImageProxy,
    ambientLightLevel: MutableState<Float>,
    isFlashOn: MutableState<Boolean>,
    camera: Camera?,
    viewModel: BarcodeScanningViewModel
) {
    if (isFrameSharp(imageProxy)) {
        if (ambientLightLevel.value < 20f && !isFlashOn.value) {
            camera?.cameraControl?.enableTorch(true)
            isFlashOn.value = true
        } else if (ambientLightLevel.value > 25f && isFlashOn.value) {
            camera?.cameraControl?.enableTorch(false)
            isFlashOn.value = false
        }
        viewModel.scanBarcodes(imageProxy)
    } else {
        imageProxy.close()
    }
}

private fun isFrameSharp(imageProxy: ImageProxy): Boolean {
    val yPlane = imageProxy.planes[0]
    val buffer = yPlane.buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)
    val luminanceValues = data.map { it.toInt() and 0xFF }
    val meanLuminance = luminanceValues.average()
    val variance =
        luminanceValues.map { (it - meanLuminance).let { diff -> diff * diff } }.average()
    return variance > 50.0
}


/*package com.pandroid.easyscan.scan

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.room.util.copy
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

@Composable
fun BarcodeScanningScreen(viewModel: BarcodeScanningViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val scanState by viewModel.scanState.collectAsState()

    // Automatically start scanning when the Composable is first launched
    LaunchedEffect(Unit) {
        viewModel.startScanning()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show camera preview if scanning is enabled
        if (scanState.isScanning) {
            CameraPreview(cameraProviderFuture, lifecycleOwner, viewModel)
        }

        // Display the scanned barcode result
        scanState.barcode?.let { barcode ->

            Text(
                text = "Barcode: ${barcode.rawValue}",
                modifier = Modifier.clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(barcode.rawValue))
                    context.startActivity(intent)
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rescan button
            ElevatedButton(onClick = { viewModel.startScanning() }) {
                Text("Rescan")
            }

        }

    }

}


@Composable
fun CameraPreview(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner,
    viewModel: BarcodeScanningViewModel
) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }
    val cameraInstance = remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    // Manage light sensor and flash state efficiently
    val isFlashOn = remember { mutableStateOf(false) }
    val ambientLightLevel = remember { mutableFloatStateOf(0f) }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    val scanLineOffset = remember { Animatable(0f) }

    DisposableEffect(Unit) {
        val lightSensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    ambientLightLevel.floatValue = event.values[0]
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Register light sensor listener
        sensorManager.registerListener(
            lightSensorListener,
            lightSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        onDispose {
            // Unregister light sensor listener
            sensorManager.unregisterListener(lightSensorListener)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            scanLineOffset.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
            scanLineOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.15f)) // Set background alpha
    ) {

        // Handle tap-to-focus and camera preview
        AndroidView(factory = { previewView }, modifier = Modifier
            .clickable {
                val factory = previewView.meteringPointFactory
                val meteringPoint =
                    factory.createPoint(previewView.width / 2f, previewView.height / 2f)
                val action = FocusMeteringAction
                    .Builder(meteringPoint)
                    .build()
                cameraInstance.value?.cameraControl?.startFocusAndMetering(action)
            }
            .fillMaxSize()
        )

        Card(
            onClick = {
                // TODO: Handle card click if needed
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(start = 65.dp, end = 65.dp, top = 100.dp)
                .size(250.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Animated scanning line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Green)
                        .offset(y = with(LocalDensity.current) { (scanLineOffset.value * 250).toDp() }) // Calculate offset based on animation
                )
            }
        }
    }


    LaunchedEffect(Unit) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (isFrameSharp(imageProxy)) {
                            // Manage flash state based on light sensor
                            if (ambientLightLevel.floatValue < 20f && !isFlashOn.value) {
                                cameraInstance.value?.cameraControl?.enableTorch(true)
                                isFlashOn.value = true
                            } else if (ambientLightLevel.floatValue > 25f && isFlashOn.value) {
                                cameraInstance.value?.cameraControl?.enableTorch(false)
                                isFlashOn.value = false
                            }

                            // Process image for barcode scanning
                            viewModel.scanBarcodes(imageProxy)
                        } else {
                            imageProxy.close() // Close blurred frames to optimize performance
                        }
                    }
                }

            try {
                // Bind use cases to lifecycle
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                cameraInstance.value = camera
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

}


private fun isFrameSharp(imageProxy: ImageProxy): Boolean {
    val yPlane = imageProxy.planes[0]
    val buffer = yPlane.buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)

    // Calculate the variance of luminance to detect sharpness
    val luminanceValues = data.map { it.toInt() and 0xFF }
    val meanLuminance = luminanceValues.average()
    val variance = luminanceValues
        .map { (it - meanLuminance).let { diff -> diff * diff } }
        .average()

    return variance > 50.0 // Adjust threshold based on your testing

}*/

