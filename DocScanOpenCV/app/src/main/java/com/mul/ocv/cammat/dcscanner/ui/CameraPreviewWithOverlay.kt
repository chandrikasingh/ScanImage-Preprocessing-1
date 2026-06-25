package com.mul.ocv.cammat.dcscanner.ui

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.util.Size
import android.view.Surface
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewWithOverlay(
	context: Context,
	lifecycleOwner: LifecycleOwner,
	modifier: Modifier = Modifier,
	onImageCaptured: ((Bitmap) -> Unit)? = null,
	onImageCaptureReady: ((ImageCapture) -> Unit)? = null
) {
	var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
	
	Box(modifier = modifier) {
		// Camera Preview (CameraX PreviewView wrapped in AndroidView)
		AndroidView(
			factory = { ctx ->
				val previewView = PreviewView(ctx).apply {
					scaleType = PreviewView.ScaleType.FILL_CENTER
					implementationMode = PreviewView.ImplementationMode.COMPATIBLE
				}
				
				val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
				cameraProviderFuture.addListener({
					val cameraProvider = cameraProviderFuture.get()
					val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
					
					val rotation = previewView.display?.rotation ?: Surface.ROTATION_0
					val preview = Preview.Builder()
						.setTargetRotation(rotation)
						.build()
					
					val analyzer = ImageAnalysis.Builder()
						.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
						.setTargetResolution(Size(1280, 720))
						.setTargetRotation(rotation)
						.build()
					
					val imageCapture = ImageCapture.Builder()
						.setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
						.setTargetRotation(rotation)
						.build()
					
					imageCaptureRef = imageCapture
					onImageCaptureReady?.invoke(imageCapture)
					
					// Analyzer currently not used (disabled for stability with overlay coords).
					val executor = Executors.newSingleThreadExecutor()
					analyzer.setAnalyzer(executor) { imageProxy ->
						try {
							// Real-time detection would happen here.
						} finally {
							imageProxy.close()
						}
					}
					
					preview.setSurfaceProvider(previewView.surfaceProvider)
					
					try {
						cameraProvider.unbindAll()
						cameraProvider.bindToLifecycle(
							lifecycleOwner,
							cameraSelector,
							preview,
							analyzer,
							imageCapture
						)
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}, ContextCompat.getMainExecutor(ctx))
				
				previewView
			},
			modifier = Modifier
				.fillMaxSize()
				.pointerInput(Unit) {
					detectTapGestures { offset ->
						// TODO: implement tap-to-focus using CameraControl from CameraX
					}
				}
		)
		
		// Static guide overlay drawn with Compose Canvas
		Canvas(modifier = Modifier.fillMaxSize()) {
			val canvasWidth = size.width
			val canvasHeight = size.height
			val centerX = canvasWidth / 2
			val centerY = canvasHeight / 2
			
			val guideRectWidth = canvasWidth * 0.8f
			val guideRectHeight = canvasHeight * 0.55f
			val guideRect = Rect(
				centerX - guideRectWidth / 2,
				centerY - guideRectHeight / 2,
				centerX + guideRectWidth / 2,
				centerY + guideRectHeight / 2
			)
			
			// Outer rectangle (stroke)
			drawRect(
				color = Color(0xFFFFFFFF),
				topLeft = Offset(guideRect.left, guideRect.top),
				size = androidx.compose.ui.geometry.Size(guideRect.width, guideRect.height),
				style = Stroke(width = 4f, miter = 4f)
			)
			
			// corner guides
			val cornerSize = 50f
			val corners = listOf(
				Pair(guideRect.left, guideRect.top),
				Pair(guideRect.right, guideRect.top),
				Pair(guideRect.right, guideRect.bottom),
				Pair(guideRect.left, guideRect.bottom)
			)
			
			corners.forEach { corner ->
				if (corner.first == guideRect.left && corner.second == guideRect.top) {
					drawLine(
						color = Color(0xFFFFFFFF),
						start = Offset(guideRect.left, guideRect.top),
						end = Offset(guideRect.left + cornerSize, guideRect.top),
						strokeWidth = 6f
					)
					drawLine(
						color = Color(0xFFFFFFFF),
						start = Offset(guideRect.left, guideRect.top),
						end = Offset(guideRect.left, guideRect.top + cornerSize),
						strokeWidth = 6f
					)
				} else if (corner.first == guideRect.right && corner.second == guideRect.top) {
					drawLine(
						color = Color(0xFFFFFFFF),
						start = Offset(guideRect.right, guideRect.top),
						end = Offset(guideRect.right - cornerSize, guideRect.top),
						strokeWidth = 6f
					)
					drawLine(
						color = Color(0xFFFFFFFF),
						start = Offset(guideRect.right, guideRect.top),
						end = Offset(guideRect.right, guideRect.top + cornerSize),
						strokeWidth = 6f
					)
				} else if (corner.first == guideRect.right && corner.second == guideRect.bottom) {
					drawLine(
						color = Color(0xFFFFFFFF),
						start = Offset(guideRect.right, guideRect.bottom),
						end = Offset(guideRect.right - cornerSize, guideRect.bottom),
						strokeWidth = 6f
					)
					drawLine(
						color = Color(0xFFFFFFFF),
						start = Offset(guideRect.right, guideRect.bottom),
						end = Offset(guideRect.right, guideRect.bottom - cornerSize),
						strokeWidth = 6f
					)
				} else if (corner.first == guideRect.left && corner.second == guideRect.bottom) {
					drawLine(
						color = Color(0xFFFFFFFF),
						start = Offset(guideRect.left, guideRect.bottom),
						end = Offset(guideRect.left + cornerSize, guideRect.bottom),
						strokeWidth = 6f
					)
					drawLine(
						color = Color(0xFFFFFFFF),
						start = Offset(guideRect.left, guideRect.bottom),
						end = Offset(guideRect.left, guideRect.bottom - cornerSize),
						strokeWidth = 6f
					)
				}
			}
		}
	}
}

/** Convert ImageProxy YUV planes to OpenCV Mat (BGR). */
@OptIn(ExperimentalGetImage::class)
fun ImageProxy.toMat(image: Image): Mat? {
	try {
		val yPlane = planes[0]
		val uPlane = planes[1]
		val vPlane = planes[2]
		
		val yBuffer = yPlane.buffer
		val uBuffer = uPlane.buffer
		val vBuffer = vPlane.buffer
		
		val ySize = yBuffer.remaining()
		val uSize = uBuffer.remaining()
		val vSize = vBuffer.remaining()
		
		val nv21 = ByteArray(ySize + uSize + vSize)
		
		yBuffer.get(nv21, 0, ySize)
		vBuffer.get(nv21, ySize, vSize)
		uBuffer.get(nv21, ySize + vSize, uSize)
		
		val yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
		yuvMat.put(0, 0, nv21)
		
		val bgrMat = Mat()
		Imgproc.cvtColor(yuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_NV21)
		
		yuvMat.release()
		return bgrMat
	} catch (e: Exception) {
		e.printStackTrace()
		return null
	}
}