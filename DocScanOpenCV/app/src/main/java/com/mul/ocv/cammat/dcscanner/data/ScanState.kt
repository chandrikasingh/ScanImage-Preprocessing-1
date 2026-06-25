package com.mul.ocv.cammat.dcscanner.data


import android.graphics.Bitmap
import org.opencv.core.Point

sealed class ScanState {
	data object Idle : ScanState() // Nothing happening yet
	data object Detecting : ScanState() // Detection running
	data class Detected(val corners: List<Point>) : ScanState() // Document corners found
	data class Cropped(val bitmap: Bitmap) : ScanState() // Final cropped document
	data class Error(val message: String) : ScanState() // Any failure
}