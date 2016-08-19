package com.monkeycoders.sl.circlerelationslayout

import android.content.Context
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.view.View

/**
 * @author Stas Lelyuk
 * *
 * @since 8/8/16
 */
class CircleView : View {

    /**
     * Stroke color
     */
    var strokeColor: Int = DEFAULT_STROKE_COLOR
        set(@ColorInt value) {
            field = value
            invalidatePaints()
        }

    /**
     * Fill color for normal state
     */
    var fillColorNormal: Int = DEFAULT_FILL_COLOR
        set(@ColorInt value) {
            field = value
            invalidatePaints()
        }

    /**
     * Fill color for selected state
     */
    var fillColorSelected: Int = DEFAULT_FILL_COLOR
        set(@ColorInt value) {
            field = value
            invalidatePaints()
        }

    /**
     * Stroke width
     */
    var strokeWidth: Float = DEFAULT_STROKE_WIDTH
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Fill radius
     */
    var fillRadius: Float = DEFAULT_FILL_RADIUS
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Icon bitmap
     */
    var icon: Bitmap? = null
        set(value) {
            field = value
            invalidate()
        }

    private val strokePaint: Paint
    private val fillPaint: Paint

    private val innerRectF: RectF

    private var viewSize = DEFAULT_VIEW_SIZE
    private var viewWidth = DEFAULT_VIEW_SIZE
    private var viewHeight = DEFAULT_VIEW_SIZE

    init {
        strokePaint = Paint()
        fillPaint = Paint()
        innerRectF = RectF()
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.CircleView, defStyle, 0)

        if (a.hasValue(R.styleable.CircleView_cv_icon)) {
            val iconId: Int = a.getResourceId(R.styleable.CircleView_cv_icon, R.drawable.ic_account_multiple_black_48dp)
            icon = BitmapFactory.decodeResource(resources, iconId)
        }

        strokeColor = a.getColor(R.styleable.CircleView_cv_strokeColor, DEFAULT_STROKE_COLOR)

        fillColorNormal = a.getColor(R.styleable.CircleView_cv_fillColor, DEFAULT_FILL_COLOR)
        fillColorSelected = a.getColor(R.styleable.CircleView_cv_fillColorSelected, DEFAULT_FILL_COLOR)

        strokeWidth = a.getFloat(R.styleable.CircleView_cv_strokeWidthSize, DEFAULT_STROKE_WIDTH)
        fillRadius = a.getFloat(R.styleable.CircleView_cv_fillRadius, DEFAULT_FILL_RADIUS)

        a.recycle()

        //Stroke Paint
        strokePaint.flags = Paint.ANTI_ALIAS_FLAG
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = strokeColor
        strokePaint.strokeWidth = strokeWidth

        //Fill Paint
        fillPaint.flags = Paint.ANTI_ALIAS_FLAG
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = if (isSelected) fillColorSelected else fillColorNormal
    }

    private fun invalidatePaints() {
        strokePaint.color = strokeColor
        fillPaint.color = if (isSelected) fillColorSelected else fillColorNormal
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        viewWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = View.MeasureSpec.getSize(heightMeasureSpec)
        viewSize = Math.max(viewWidth, viewHeight)

        setMeasuredDimension(viewWidth, viewHeight)
    }

    override fun onDraw(canvas: Canvas) {

        innerRectF.set(0f, 0f, viewSize.toFloat(), viewSize.toFloat())
        innerRectF.offset(((width - viewSize) / 2).toFloat(), ((height - viewSize) / 2).toFloat())

        val halfBorder = (strokePaint.strokeWidth / 2f + 0.5f).toInt()

        innerRectF.inset(halfBorder.toFloat(), halfBorder.toFloat())

        val centerX = innerRectF.centerX()
        val centerY = innerRectF.centerY()

        //canvas.drawArc(innerRectF, 0f, 360f, true, mBackgroundPaint!!)

        val radius = viewSize / 2 * fillRadius

        canvas.drawCircle(centerX, centerY, radius + 0.5f - strokePaint.strokeWidth, fillPaint)
        canvas.drawOval(innerRectF, strokePaint)

        if (icon != null) {
            val cx = (viewWidth - paddingLeft - paddingRight - icon!!.width) shr 1
            val cy = (viewHeight - paddingTop - paddingBottom - icon!!.height) shr 1
            canvas.drawBitmap(icon, cx.toFloat(), cy.toFloat(), null)
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        invalidatePaints()
    }

    /**
     * Sets the view's fill color.
     *
     * @param normal
     * @param selected
     */
    fun fillColor(@ColorInt normal: Int, @ColorInt selected: Int) {
        fillColorNormal = normal
        fillColorSelected = selected
        invalidatePaints()
    }

    companion object {
        private @ColorInt val DEFAULT_STROKE_COLOR = Color.TRANSPARENT
        private @ColorInt val DEFAULT_FILL_COLOR = Color.GRAY

        private val DEFAULT_STROKE_WIDTH = 0.0f
        private val DEFAULT_FILL_RADIUS = 1.0f

        private val DEFAULT_VIEW_SIZE = 96
    }
}