package com.mul.ocv.cammat.dcscanner

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import org.opencv.android.OpenCVLoader
import com.mul.ocv.cammat.dcscanner.ui.DocumentScannerScreen
import com.mul.ocv.cammat.dcscanner.ui.theme.DCScannerTheme

const val TAG = "DocScanner"


class ContentMainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		enableEdgeToEdge()
		
		val requestPermissionLauncher =
			registerForActivityResult(ActivityResultContracts.RequestPermission())
			{ isGranted ->
				if (isGranted) {
					setScannerContent()
				} else {
					finish()
				}
			}
		
		when {
			ContextCompat.checkSelfPermission(
				this,
				android.Manifest.permission.CAMERA
			) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
				setScannerContent()
			}
			else -> {
				requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
			}
		}
		
	}
	
	private fun setScannerContent() {
		if (!OpenCVLoader.initDebug()) {
			Log.e(TAG, "Unable to load OpenCV");
		} else {
			Log.d(TAG, "OpenCV loaded successfully");
		}
		setContent {
			DCScannerTheme {
				Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier) {
					DocumentScannerScreen()
				}
			}
		}
	}
	
}













