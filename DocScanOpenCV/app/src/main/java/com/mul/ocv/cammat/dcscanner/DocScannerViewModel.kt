package com.mul.ocv.cammat.dcscanner

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mul.ocv.cammat.dcscanner.data.EdgeDetector
import com.mul.ocv.cammat.dcscanner.data.ImageEnhancer
import com.mul.ocv.cammat.dcscanner.data.PerspectiveTransformer
import com.mul.ocv.cammat.dcscanner.data.ScanState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.opencv.core.Point

class DocScannerViewModel: ViewModel() {
	private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
	val scanState: StateFlow<ScanState> = _scanState
	var lastCapturedBitmap: Bitmap? = null
		private set
	
	fun detectDocument(bitmap: Bitmap, autoDetect: Boolean = true) {
		viewModelScope.launch {
			lastCapturedBitmap = bitmap
			_scanState.value = ScanState.Detecting
			try {
				val corners = EdgeDetector.detectEdges(bitmap)
				if (corners != null && corners.size == 4) {
					if (autoDetect) {
						val tightenedCorners = tightenCrop(bitmap, corners, 0.96f)
						val cropped = PerspectiveTransformer.warpPerspective(bitmap, tightenedCorners)
						if (cropped != null) {
							val enhanced = ImageEnhancer.enhanceImage(cropped)
							_scanState.value = ScanState.Cropped(enhanced)
						} else {
							_scanState.value = ScanState.Error("Cropping failed.")
						}
					} else {
						_scanState.value = ScanState.Detected(corners)
					}
				} else {
					_scanState.value = ScanState.Error("No document detected. Try adjusting the angle or lighting.")
				}
			} catch (e: Exception) {
				e.printStackTrace()
				_scanState.value = ScanState.Error("Detection failed: ${e.message}")
			}
		}
	}
	
	fun cropDocument(bitmap: Bitmap, corners: List<Point>) {
		viewModelScope.launch {
			try {
				_scanState.value = ScanState.Detecting
				val cropped = PerspectiveTransformer.warpPerspective(bitmap, corners)
				if (cropped != null) {
					val enhanced = ImageEnhancer.enhanceImage(cropped)
					_scanState.value = ScanState.Cropped(enhanced)
				} else {
					_scanState.value = ScanState.Error("Cropping failed.")
				}
			} catch (e: Exception) {
				e.printStackTrace()
				_scanState.value = ScanState.Error("Processing failed: ${e.message}")
			}
		}
	}
	
	fun tightenCrop(bitmap: Bitmap, corners: List<Point>, tightness: Float = 0.98f): List<Point> {
		val centerX = corners.map { it.x }.average()
		val centerY = corners.map { it.y }.average()
		return corners.map { point ->
			val dx = point.x - centerX
			val dy = point.y - centerY
			Point((centerX + dx * tightness), (centerY + dy * tightness))
		}
	}
	
	fun reset() {
		lastCapturedBitmap = null
		_scanState.value = ScanState.Idle
	}
}