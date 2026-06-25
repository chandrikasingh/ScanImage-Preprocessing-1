package com.mul.ocv.cammat.dcscanner.data

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

object ImageEnhancer {
	
	/**
	 * Enhance image quality with brightness, contrast, and sharpening
	 */
	fun enhanceImage(bitmap: Bitmap): Bitmap {
		val src = Mat()
		Utils.bitmapToMat(bitmap, src)
		
		// Normalize to BGR
		val bgr = Mat()
		if (src.channels() == 4) {
			Imgproc.cvtColor(src, bgr, Imgproc.COLOR_RGBA2BGR)
		} else {
			src.copyTo(bgr)
		}
		
		// Apply automatic brightness and contrast using adaptive equalization
		val enhanced = Mat()
		
		// 1. Convert to LAB color space
		val lab = Mat()
		Imgproc.cvtColor(bgr, lab, Imgproc.COLOR_BGR2Lab)
		
		// 2. Split channels
		val channels = mutableListOf<Mat>()
		org.opencv.core.Core.split(lab, channels)
		
		// 3. Apply CLAHE to L channel for brightness and contrast
		val clahe = Imgproc.createCLAHE(3.0, org.opencv.core.Size(8.0, 8.0))
		val lChannel = channels[0]
		val enhancedL = Mat()
		clahe.apply(lChannel, enhancedL)
		
		// 4. Increase brightness by scaling L channel
		val brighterL = Mat()
		enhancedL.convertTo(brighterL, -1, 1.2, 10.0) // Scale by 1.2x and add 10 for brightness
		
		// Update L channel
		channels[0] = brighterL
		
		// 5. Merge channels back
		val enhancedLab = Mat()
		org.opencv.core.Core.merge(channels, enhancedLab)
		
		// 6. Convert back to BGR
		Imgproc.cvtColor(enhancedLab, enhanced, Imgproc.COLOR_Lab2BGR)
		
		// 7. Apply unsharp masking for sharpness
		val blurred = Mat()
		Imgproc.GaussianBlur(enhanced, blurred, org.opencv.core.Size(5.0, 5.0), 2.0)
		val sharpened = Mat()
		org.opencv.core.Core.addWeighted(enhanced, 1.5, blurred, -0.5, 0.0, sharpened)
		
		// 8. Convert back to bitmap
		val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
		Utils.matToBitmap(sharpened, resultBitmap)
		
		// Release mats
		src.release()
		bgr.release()
		lab.release()
		channels.forEach { it.release() }
		enhancedL.release()
		brighterL.release()
		enhancedLab.release()
		enhanced.release()
		blurred.release()
		sharpened.release()
		
		return resultBitmap
	}
	
	/**
	 * Convert to grayscale with improved contrast
	 */
	fun toGrayscale(bitmap: Bitmap): Bitmap {
		val src = Mat()
		Utils.bitmapToMat(bitmap, src)
		
		val gray = Mat()
		if (src.channels() == 4) {
			Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY)
		} else {
			Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
		}
		
		// Enhance contrast
		val enhanced = Mat()
		Imgproc.equalizeHist(gray, enhanced)
		
		val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
		Utils.matToBitmap(enhanced, resultBitmap)
		
		src.release()
		gray.release()
		enhanced.release()
		
		return resultBitmap
	}
}