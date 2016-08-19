package com.monkeycoders.sl.circlerelationslayout

import android.graphics.Path
import android.graphics.PointF

/**
 * @author Stas Lelyuk
 * @since 8/9/16
 */
class RelationPath(
        val from: PointF,
        val to: PointF,
        val center: PointF,
        val radius: Int) {

    fun buildPath(): Path {
        //val dx: Float = (center.x + from.x) / 2
        //val dy: Float = (center.y + from.y) / 2
        val path: Path = Path()
        path.moveTo(from.x, from.y)
        path.quadTo(center.x, center.y, to.x, to.y)
        return path
    }

    fun buildPath(multiplier: Long): Path {
        val path: Path = Path()
        path.moveTo(from.x, from.y)
        path.quadTo(center.x, center.y, to.x * multiplier, to.y * multiplier)
        return path
    }

    fun recalculateStartPoint(from: PointF): PointF {
        val hypotenuseBig = StrictMath.hypot(from.x.toDouble(), from.y.toDouble())
        val hypotenuseSmall = hypotenuseBig - radius
        val sinA = from.y / hypotenuseBig.toDouble()
        val startY: Double = sinA * hypotenuseSmall
        val cosA: Double = StrictMath.sqrt(1.0 - StrictMath.pow(sinA, 2.0))
        val startX: Double = cosA * hypotenuseSmall
        return PointF(startX.toFloat(), startY.toFloat())
    }
}