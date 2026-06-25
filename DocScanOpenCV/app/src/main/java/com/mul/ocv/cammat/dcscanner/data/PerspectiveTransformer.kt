package com.mul.ocv.cammat.dcscanner.data

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object PerspectiveTransformer {
	
	fun warpPerspective(inputBitmap: Bitmap, corners: List<Point>): Bitmap? {
		if (corners.size != 4) return null
		
		// Sort corners: TL, TR, BR, BL
		val sorted = sortCornersTLTRBRBL(corners)
		
		// Calculate dimensions of the destination image
		val widthA = distance(sorted[0], sorted[1])
		val widthB = distance(sorted[2], sorted[3])
		val maxWidth = maxOf(widthA, widthB).toInt()
		
		val heightA = distance(sorted[1], sorted[2])
		val heightB = distance(sorted[0], sorted[3])
		val maxHeight = maxOf(heightA, heightB).toInt()
		
		val srcMat = MatOfPoint2f(
			sorted[0], sorted[1], sorted[2], sorted[3]
		)
		
		val dstMat = MatOfPoint2f(
			Point(0.0, 0.0),
			Point(maxWidth.toDouble() - 1, 0.0),
			Point(maxWidth.toDouble() - 1, maxHeight.toDouble() - 1),
			Point(0.0, maxHeight.toDouble() - 1)
		)
		
		val transform = Imgproc.getPerspectiveTransform(srcMat, dstMat)
		
		val src = Mat()
		Utils.bitmapToMat(inputBitmap, src)
		// Ensure 3-channel BGR for warp
		val srcBgr = Mat()
		if (src.channels() == 4) {
			Imgproc.cvtColor(src, srcBgr, Imgproc.COLOR_RGBA2BGR)
		} else {
			src.copyTo(srcBgr)
		}
		
		val dst = Mat(Size(maxWidth.toDouble(), maxHeight.toDouble()), CvType.CV_8UC4)
		Imgproc.warpPerspective(srcBgr, dst, transform, Size(maxWidth.toDouble(), maxHeight.toDouble()))
		
		val resultBitmap = Bitmap.createBitmap(maxWidth, maxHeight, Bitmap.Config.ARGB_8888)
		Utils.matToBitmap(dst, resultBitmap)
		
		src.release()
		srcBgr.release()
		dst.release()
		transform.release()
		srcMat.release()
		dstMat.release()
		
		return resultBitmap
	}
	
	private fun distance(p1: Point, p2: Point): Double {
		return Math.hypot(p1.x - p2.x, p1.y - p2.y)
	}
	
	/**
	 * Sort TL, TR, BR, BL using sum/diff heuristic (most robust for rectangles).
	 */
	fun sortCornersTLTRBRBL(points: List<Point>): List<Point> {
		require(points.size == 4)
		val sumSorted = points.sortedBy { it.x + it.y }
		val diffSorted = points.sortedBy { it.y - it.x }
		
		val topLeft = sumSorted.first()
		val bottomRight = sumSorted.last()
		val topRight = diffSorted.first()
		val bottomLeft = diffSorted.last()
		
		return listOf(topLeft, topRight, bottomRight, bottomLeft)
	}
}