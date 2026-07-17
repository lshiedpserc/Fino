package com.example.fino.ui.scan

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.fino.data.AppDao
import com.example.fino.data.TransactionEntity
import com.example.fino.ui.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(dao: AppDao, onNavigateToHistory: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var scannedAmount by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Camera Preview
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    coroutineScope.launch {
                        val cameraProvider = ctx.getCameraProvider()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        imageCapture = ImageCapture.Builder().build()
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraPreview", "Use case binding failed", exc)
                        }
                    }
                    previewView
                }
            )

            // Viewfinder Overlay
            Box(modifier = Modifier.fillMaxSize()) {
                // Dark overlay outside viewfinder - approximated by a border for simplicity here
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.85f)
                        .aspectRatio(3f/4f)
                        .border(2.dp, OutlineVariant, RoundedCornerShape(12.dp))
                ) {
                    // Corner brackets
                    Box(modifier = Modifier.align(Alignment.TopStart).size(32.dp).border(4.dp, PrimaryFixedDim, RoundedCornerShape(topStart = 12.dp)))
                    Box(modifier = Modifier.align(Alignment.TopEnd).size(32.dp).border(4.dp, PrimaryFixedDim, RoundedCornerShape(topEnd = 12.dp)))
                    Box(modifier = Modifier.align(Alignment.BottomStart).size(32.dp).border(4.dp, PrimaryFixedDim, RoundedCornerShape(bottomStart = 12.dp)))
                    Box(modifier = Modifier.align(Alignment.BottomEnd).size(32.dp).border(4.dp, PrimaryFixedDim, RoundedCornerShape(bottomEnd = 12.dp)))
                }

                Text(
                    text = "枠内にレシートを合わせてください",
                    color = PrimaryFixedDim,
                    style = AppTypography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 120.dp)
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp, start = 32.dp, end = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isProcessing) return@Button
                        isProcessing = true
                        imageCapture?.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(image)
                                    image.close()
                                    processImage(bitmap) { amount ->
                                        if (amount != null) {
                                            scannedAmount = amount
                                            coroutineScope.launch {
                                                dao.insertTransaction(
                                                    TransactionEntity(
                                                        title = "レシートスキャン",
                                                        amount = amount,
                                                        category = "その他",
                                                        date = System.currentTimeMillis(),
                                                        isIncome = false,
                                                        iconName = "R"
                                                    )
                                                )
                                                isProcessing = false
                                                onNavigateToHistory()
                                            }
                                        } else {
                                            isProcessing = false
                                            // Show error toast or something
                                        }
                                    }
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    Log.e("CameraCapture", "Photo capture failed: ${exception.message}", exception)
                                    isProcessing = false
                                }
                            }
                        )
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryFixedDim),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text("S", color = OnPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("カメラの権限が必要です", color = OnSurface)
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    val matrix = Matrix()
    matrix.postRotate(image.imageInfo.rotationDegrees.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun processImage(bitmap: Bitmap, onResult: (Double?) -> Unit) {
    val image = InputImage.fromBitmap(bitmap, 0)
    val recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val text = visionText.text
            Log.d("MLKit", "Extracted text: \n$text")

            // Simple heuristic to find Total (合計) amount
            // Look for lines containing "合" or "計" or "Total", then extract the largest number near it, or just find the largest number overall as a fallback.
            var maxAmount = 0.0
            val numberRegex = Regex("[0-9,]+")

            val lines = text.split("\n")
            for (line in lines) {
                if (line.contains("合") || line.contains("計") || line.contains("TOTAL", ignoreCase = true)) {
                    val numbers = numberRegex.findAll(line).map { it.value.replace(",", "").toDoubleOrNull() ?: 0.0 }.toList()
                    if (numbers.isNotEmpty()) {
                        val maxInLine = numbers.maxOrNull() ?: 0.0
                        if (maxInLine > maxAmount) maxAmount = maxInLine
                    }
                }
            }

            // Fallback: If no "Total" keyword found, just take the largest number
            if (maxAmount == 0.0) {
                 val allNumbers = numberRegex.findAll(text).map { it.value.replace(",", "").toDoubleOrNull() ?: 0.0 }.toList()
                 if (allNumbers.isNotEmpty()) {
                     maxAmount = allNumbers.maxOrNull() ?: 0.0
                 }
            }

            onResult(if (maxAmount > 0) maxAmount else null)
        }
        .addOnFailureListener { e ->
            Log.e("MLKit", "Text recognition failed", e)
            onResult(null)
        }
}
