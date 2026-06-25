package com.mul.ocv.cammat.dcscanner.data

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

object EdgeDetector {
	
	/**
	 * Detects the four corners of a document in a given bitmap.
	 * @return List of 4 corner points if detected, null otherwise.
	 */
	fun detectEdges(bitmap: Bitmap): List<Point>? {
		val src = Mat()
		Utils.bitmapToMat(bitmap, src)
		
		// Convert to grayscale
		val gray = Mat()
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
		
		// Blur to reduce noise
		Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
		
		// Detect edges using Canny
		val edges = Mat()
		Imgproc.Canny(gray, edges, 75.0, 200.0)
		
		// Find contours
		val contours = ArrayList<MatOfPoint>()
		val hierarchy = Mat()
		Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
		
		// Sort contours by area, descending
		contours.sortByDescending { Imgproc.contourArea(it) }
		
		var docContour: MatOfPoint2f? = null
		
		for (contour in contours) {
			val peri = Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true)
			val approx = MatOfPoint2f()
			Imgproc.approxPolyDP(MatOfPoint2f(*contour.toArray()), approx, 0.02 * peri, true)
			
			// Look for a 4-point contour (document shape)
			if (approx.total() == 4L) {
				docContour = approx
				break
			}
		}
		
		gray.release()
		edges.release()
		src.release()
		
		return docContour?.toList()
	}
}