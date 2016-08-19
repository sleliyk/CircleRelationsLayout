/**
 * @author Stas Lelyuk
 * @since 8/8/16
 */
package com.monkeycoders.sl.circlerelationslayout

import android.graphics.PointF
import android.view.View
import android.view.ViewGroup

/**
 * @return The effective radius of the view, if it were round
 */
val View.radius: Int
    get() = Math.max(measuredWidth, measuredHeight) / 2

/**
 * Lays out a view so that its center will be on `cx` and `cy`
 * @param cx   The X coordinate of the location in the parent in which to place the view
 * @param cy   The Y coordinate of the location in the parent in which to place the view
 */
fun View.layoutFromCenter(cx: Int, cy: Int) {
    val left = cx - measuredWidth / 2
    val top = cy - measuredHeight / 2
    val right = left + measuredWidth
    val bottom = top + measuredHeight
    layout(left, top, right, bottom)
}

fun View.center(): PointF {
    val displayAreaLeft = left + paddingLeft
    val displayAreaTop = top + paddingTop
    val displayAreaRight = right - paddingRight
    val displayAreaBottom = bottom - paddingBottom

    val displayAreaWidth = displayAreaRight - displayAreaLeft
    val displayAreaHeight = displayAreaBottom - displayAreaTop
    val centerX: Float = (paddingLeft + displayAreaWidth / 2).toFloat()
    val centerY: Float = (paddingRight + displayAreaHeight / 2).toFloat()
    return PointF(centerX, centerY)
}

inline fun ViewGroup.forEachChild(action: View.() -> Unit): Unit {
    repeat(childCount, { index ->
        getChildAt(index).action()
    })
}