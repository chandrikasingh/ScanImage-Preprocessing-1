package com.mul.ocv.cammat.dcscanner.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mul.ocv.cammat.dcscanner.DocScannerViewModel
import com.mul.ocv.cammat.dcscanner.TAG
import com.mul.ocv.cammat.dcscanner.data.ScanState
import java.io.File

@Composable
fun DocumentScannerScreen(viewModel: DocScannerViewModel = viewModel()) {
	
	val context = LocalContext.current
	val scanState by viewModel.scanState.collectAsState()
	val lifecycleOwner = LocalLifecycleOwner.current
	var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
	
	LaunchedEffect(Unit) {
		viewModel.reset()
	}
	
	when (val currentState = scanState) {
		is ScanState.Cropped -> {
			CroppedImageView(
				bitmap = currentState.bitmap,
				onRetake = { viewModel.reset() },
				onSave = {
					// TODO: Implement save/share
				}
			)
		}
		
		else -> {
			Column(modifier = Modifier.fillMaxSize()) {
				Box(modifier = Modifier.weight(1f)) {
					CameraPreviewWithOverlay(
						context = context,
						lifecycleOwner = lifecycleOwner,
						modifier = Modifier.fillMaxSize(),
						onImageCaptureReady = { capture ->
							imageCapture = capture
						}
					)
					
					when (scanState) {
						is ScanState.Detecting -> {
							Box(
								modifier = Modifier.fillMaxSize(),
								contentAlignment = Alignment.Center
							) {
								CircularProgressIndicator()
							}
						}
						
						is ScanState.Detected -> {
							Box(
								modifier = Modifier.fillMaxSize(),
								contentAlignment = Alignment.BottomCenter
							) {
								Card(
									modifier = Modifier.padding(16.dp),
									colors = CardDefaults.cardColors(
										containerColor = MaterialTheme.colorScheme.surface
									)
								) {
									Column(
										modifier = Modifier.padding(16.dp),
										horizontalAlignment = Alignment.CenterHorizontally
									) {
										Text(
											"Document detected!",
											style = MaterialTheme.typography.titleSmall
										)
										Spacer(modifier = Modifier.height(8.dp))
										Text(
											"Corner coordinates:",
											style = MaterialTheme.typography.bodySmall
										)
										(scanState as ScanState.Detected).corners.forEachIndexed { index, corner ->
											Text(
												"Corner ${index + 1}: (${corner.x.toInt()}, ${corner.y.toInt()})",
												style = MaterialTheme.typography.bodySmall,
												color = MaterialTheme.colorScheme.primary
											)
										}
										Spacer(modifier = Modifier.height(16.dp))
										
										Button(
											onClick = {
												viewModel.lastCapturedBitmap?.let { bitmap ->
													val corners = (scanState as ScanState.Detected).corners
													viewModel.cropDocument(bitmap, corners)
												}
											},
											modifier = Modifier.fillMaxWidth()
										) {
											Text("Crop & Enhance")
										}
										
										Spacer(modifier = Modifier.height(8.dp))
										
										OutlinedButton(
											onClick = { viewModel.reset() },
											modifier = Modifier.fillMaxWidth()
										) {
											Text("Cancel")
										}
									}
								}
							}
						}
						
						is ScanState.Error -> {
							Snackbar(
								modifier = Modifier
									.align(Alignment.TopCenter)
									.padding(16.dp),
								action = {
									TextButton(onClick = { viewModel.reset() }) {
										Text("OK")
									}
								}
							) {
								Text((scanState as ScanState.Error).message)
							}
						}
						
						else -> {}
					}
				}
				
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(24.dp),
					horizontalArrangement = Arrangement.SpaceEvenly,
					verticalAlignment = Alignment.CenterVertically
				) {
					Button(
						onClick = { viewModel.reset() },
						colors = ButtonDefaults.buttonColors(
							containerColor = MaterialTheme.colorScheme.secondary
						)
					) {
						Icon(Icons.Default.Refresh, contentDescription = null)
						Spacer(modifier = Modifier.width(8.dp))
						Text("Reset")
					}
					
					Spacer(modifier = Modifier.width(16.dp))
					
					Button(
						onClick = {
							imageCapture?.let { capture ->
								captureImage(context, capture) { bitmap ->
									viewModel.detectDocument(bitmap, autoDetect = true)
								}
							}
						},
						enabled = imageCapture != null,
						modifier = Modifier
							.weight(1f)
							.height(56.dp)
					) {
						Text("Auto Scan")
					}
					
					Spacer(modifier = Modifier.width(8.dp))
					
					OutlinedButton(
						onClick = {
							imageCapture?.let { capture ->
								captureImage(context, capture) { bitmap ->
									viewModel.detectDocument(bitmap, autoDetect = false)
								}
							}
						},
						enabled = imageCapture != null,
						modifier = Modifier
							.weight(1f)
							.height(56.dp)
					) {
						Text("Manual Scan")
					}
				}
				
				Text(
					text = "Auto Scan = Instant crop, Manual Scan = Adjust corners",
					style = MaterialTheme.typography.bodySmall,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 8.dp),
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
				)
			}
		}
	}
}

fun captureImage(
	context: Context,
	imageCapture: ImageCapture,
	onSuccess: (Bitmap) -> Unit
) {
	val photoFile = File(context.externalCacheDir, "scan_${System.currentTimeMillis()}.jpg")
	val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
	
	imageCapture.takePicture(
		outputOptions,
		ContextCompat.getMainExecutor(context),
		object : ImageCapture.OnImageSavedCallback {
			override fun onImageSaved(output: ImageCapture.OutputFileResults) {
				try {
					val path = photoFile.absolutePath
					val options = BitmapFactory.Options().apply {
						inJustDecodeBounds = false
					}
					val bitmap = BitmapFactory.decodeFile(path, options)
					if (bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
						try {
							val exif = ExifInterface(path)
							val orientation = exif.getAttributeInt(
								ExifInterface.TAG_ORIENTATION,
								ExifInterface.ORIENTATION_NORMAL
							)
							val rotated = when (orientation) {
								ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
								ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
								ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
								else -> bitmap
							}
							onSuccess(rotated)
						} catch (e: Exception) {
							e.printStackTrace()
							onSuccess(bitmap)
						}
					} else {
						Log.e(TAG, "Failed to decode image or invalid dimensions")
					}
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
			
			override fun onError(exception: ImageCaptureException) {
				exception.printStackTrace()
			}
		}
	)
}

private fun rotateBitmap(src: Bitmap, angle: Float): Bitmap {
	val matrix = Matrix()
	matrix.postRotate(angle)
	return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
}

@Composable
fun CroppedImageView(
	bitmap: Bitmap,
	onRetake: () -> Unit,
	onSave:() -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f),
			elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
		) {
			Image(
				bitmap = bitmap.asImageBitmap(),
				contentDescription = "Scanned document",
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Fit
			)
		}
		
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			OutlinedButton(
				onClick = onRetake,
				modifier = Modifier.weight(1f)
			) {
				Icon(Icons.Default.Refresh, contentDescription = null)
				Spacer(modifier = Modifier.width(8.dp))
				Text("Scan Again")
			}
			
			Spacer(modifier = Modifier.width(16.dp))
			
			Button(
				onClick = onSave,
				modifier = Modifier.weight(1f)
			) {
				Icon(Icons.Default.Check, contentDescription = null)
				Spacer(modifier = Modifier.width(8.dp))
				Text("Save")
			}
		}
	}
}
