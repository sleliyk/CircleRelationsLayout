package com.monkeycoders.sl.circlerelationslayout

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Stas Lelyuk
 * *
 * @since 8/8/16
 */
class CircleLayout
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

    val connectorsActivePaint: Paint = Paint()
    val connectorsPaint: Paint = Paint()
    var activeChildIndex: Int = 0

    var angle: Float
    var angleOffset: Float
    var fixedRadius: Int
    var radiusPreset = FITS_SMALLEST_CHILD
        set(newRadiusPreset: Int) = when (newRadiusPreset) {
            FITS_LARGEST_CHILD, FITS_SMALLEST_CHILD -> field = newRadiusPreset
            else -> throw IllegalArgumentException("radiusPreset must be either FITS_LARGEST_CHILD or FITS_SMALLEST_CHILD")
        }
    var direction = COUNTER_CLOCKWISE
        set(newDirection: Int) = when {
            newDirection > 0 -> field = 1
            newDirection < 0 -> field = -1
            else -> throw IllegalArgumentException("direction must be either positive or negative")
        }

    val layoutHasCenterView: Boolean
        get() = centerViewId != View.NO_ID

    private var centerViewId: Int
    var centerView: View? = null
        set(newCenterView: View?) = when {
            newCenterView != null && indexOfChild(newCenterView) == -1 -> {
                throw IllegalArgumentException("View with ID ${newCenterView.id} is not a child of this layout")
            }
            else -> {
                field = newCenterView
                centerViewId = newCenterView?.id ?: NO_ID
            }
        }

    private val childrenToLayout = LinkedList<View>()
    private val circles = ConcurrentHashMap<Int, CircleItem>()

    private val childClickListener = View.OnClickListener {
        activeChildIndex = indexOfChild(it)
        buildRelationsList()
        invalidate()
    }

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.CircleLayout, defStyleAttr, 0)
        centerViewId = attributes.getResourceId(R.styleable.CircleLayout_cl_centerView, NO_ID)
        angle = Math.toRadians(attributes.getFloat(R.styleable.CircleLayout_cl_angle, 0f).toDouble()).toFloat()
        angleOffset = Math.toRadians(attributes.getFloat(R.styleable.CircleLayout_cl_angleOffset, 0f).toDouble()).toFloat()
        fixedRadius = attributes.getDimensionPixelSize(R.styleable.CircleLayout_cl_radius, 0)
        radiusPreset = attributes.getInt(R.styleable.CircleLayout_cl_radiusPreset, FITS_LARGEST_CHILD)
        direction = attributes.getInt(R.styleable.CircleLayout_cl_direction, COUNTER_CLOCKWISE)
        attributes.recycle()

        connectorsPaint.color = Color.GRAY
        connectorsPaint.strokeWidth = 1f
        connectorsPaint.isDither = true
        connectorsPaint.style = Paint.Style.STROKE
        connectorsPaint.strokeJoin = Paint.Join.BEVEL
        connectorsPaint.strokeCap = Paint.Cap.ROUND
        connectorsPaint.pathEffect = CornerPathEffect(15f)
        connectorsPaint.isAntiAlias = true
        connectorsPaint.alpha = 96

        connectorsActivePaint.set(connectorsPaint)
        connectorsActivePaint.color = Color.RED
        connectorsActivePaint.alpha = 255
        connectorsActivePaint.strokeWidth = 3f

//        valueAnimator = ValueAnimator.ofInt(0, 100)
//        valueAnimator.duration = ANIMATION_DURATION
//        valueAnimator.interpolator = LinearInterpolator()
//        valueAnimator.addUpdateListener {
//            //Log.e("Anim", "t:" + it.currentPlayTime + ", v:" + it.animatedValue)
//            invalidate()
//        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        centerView = findViewById(centerViewId)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val displayAreaLeft = left + paddingLeft
        val displayAreaTop = top + paddingTop
        val displayAreaRight = right - paddingRight
        val displayAreaBottom = bottom - paddingBottom

        val displayAreaWidth = displayAreaRight - displayAreaLeft
        val displayAreaHeight = displayAreaBottom - displayAreaTop
        val centerX = paddingLeft + displayAreaWidth / 2
        val centerY = paddingRight + displayAreaHeight / 2
        val outerRadius = Math.min(displayAreaWidth, displayAreaHeight) / 2

        centerView?.layoutFromCenter(centerX, centerY)

        var minChildRadius = outerRadius
        var maxChildRadius = 0
        childrenToLayout.clear()
        forEachChild {
            if (layoutHasCenterView && id == centerViewId || visibility == GONE) {
                return@forEachChild
            }
            childrenToLayout.add(this)
            maxChildRadius = Math.max(maxChildRadius, radius)
            minChildRadius = Math.min(minChildRadius, radius)
        }
        //choose angle increment
        val angleIncrement = if (angle != 0f) angle else getEqualAngle(childrenToLayout.size)

        //choose radius
        val layoutRadius = if (fixedRadius != 0) fixedRadius else getLayoutRadius(outerRadius, maxChildRadius, minChildRadius)

        layoutChildrenAtAngle(centerX, centerY, angleIncrement, angleOffset, layoutRadius, childrenToLayout)
        post { buildRelationsList() }
    }

    override fun addView(child: View?) {
        super.addView(child)

        child?.setOnClickListener(childClickListener)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        circles.forEach {
            drawCircleRelations(canvas, it.value)
        }
    }

    private fun drawCircleRelations(canvas: Canvas, circle: CircleItem) {
        val p = circle.buildPath()
        canvas.drawPath(p, if (circle.active) connectorsActivePaint else connectorsPaint)
    }

    fun addCircle(circleItem: CircleItem) {
        if (checkExistingCircles(circleItem.id))
            return

        // TODO Use CircleView directly
        val circleView = LayoutInflater.from(context).inflate(R.layout.item_child, this, false)
        addView(circleView)
        circles.put(indexOfChild(circleView), circleItem)
    }

    // TODO
    fun setCircles(circles: List<CircleItem>) {
        circles.forEach { addCircle(it) }
    }

    private fun buildRelationsList() {
//        circles.clear()

        val center = center()

        val circlesClone: ArrayList<Int> = ArrayList(circles.keys)
        (0..circles.size - 1).forEach {
            val key: Int = circlesClone[it]
            val c: CircleItem = circles[key]!!

            val ch1: View = getChildAt(key)
            val r1 = Rect()
            ch1.getHitRect(r1)

            val isActive = key == activeChildIndex

            val startX = r1.exactCenterX()
            val startY = r1.exactCenterY()

            c.active = isActive

            (0..c.relationsToCircles.size - 1).forEach inner@ { k ->
                val id: Int = c.relationsToCircles[k]
                val circleViewIndex = findCircleIndexById(id)
                val relatedCircle: CircleItem = circles[circleViewIndex] ?: return@inner

                if (relatedCircle.active || id == c.id) {
                    return@inner
                }

                val ch2: View = getChildAt(circleViewIndex)

                val r2 = Rect()
                ch2.getHitRect(r2)

                val endX: Float = r2.exactCenterX()
                val endY: Float = r2.exactCenterY()

                c.addRelation(id,
                        RelationPath(
                                PointF(startX, startY), PointF(endX, endY),
                                PointF(center.x, center.y),
                                ch1.radius))
            }
            ch1.isSelected = isActive
        }
    }

    private fun findCircleIndexById(id: Int): Int = circles.filter({ it.value.id == id }).keys.last()

    private fun checkExistingCircles(id: Int): Boolean = circles.filterValues { it.id == id }.isNotEmpty()

    /**
     * @param outerRadius The outer radius of this layout's display area
     * @param maxChildRadius The radius of the largest child
     * @param minChildRadius The radius of the smallest child
     * @return The radius of the layout path along which the children will be placed
     */
    private fun getLayoutRadius(outerRadius: Int, maxChildRadius: Int, minChildRadius: Int): Int {
        return when (radiusPreset) {
            FITS_LARGEST_CHILD -> outerRadius - maxChildRadius
            FITS_SMALLEST_CHILD -> outerRadius - minChildRadius
            else -> outerRadius - maxChildRadius
        }
    }

    /**
     * Splits a circle into `n` equal slices
     * @param numSlices The number of slices in which to divide the circle
     * @return The angle between two adjacent slices, or 2*pi if `n` is zero
     */
    private fun getEqualAngle(numSlices: Int): Float = 2 * Math.PI.toFloat() / if (numSlices != 0) numSlices else 1

    /**
     * Lays out the child views along a circle
     * @param cx                    The X coordinate of the center of the circle
     * @param cy                    The Y coordinate of the center of the circle
     * @param angleIncrement        The angle increment between two adjacent children
     * @param angleOffset           The starting offset angle from the horizontal axis
     * @param radius                The radius of the circle along which the centers of the children will be placed
     * @param childrenToLayout      The views to layout
     */
    private fun layoutChildrenAtAngle(cx: Int, cy: Int, angleIncrement: Float, angleOffset: Float, radius: Int, childrenToLayout: List<View>) {
        var currentAngle = angleOffset
        childrenToLayout.forEach {
            val childCenterX = polarToX(radius.toFloat(), currentAngle)
            val childCenterY = polarToY(radius.toFloat(), currentAngle)
            it.layoutFromCenter(cx + childCenterX, cy - childCenterY)

            currentAngle += angleIncrement * direction
        }
    }

    /**
     * Gets the X coordinate from a set of polar coordinates
     * @param radius The polar radius
     * @param angle  The polar angle
     * @return The equivalent X coordinate
     */
    fun polarToX(radius: Float, angle: Float): Int = (radius * Math.cos(angle.toDouble())).toInt()

    /**
     * Gets the Y coordinate from a set of polar coordinates
     * @param radius The polar radius
     * @param angle  The polar angle
     * @return The equivalent Y coordinate
     */
    fun polarToY(radius: Float, angle: Float): Int = (radius * Math.sin(angle.toDouble())).toInt()


    companion object {
        /**
         * The type of override for the radius of the circle
         */
        private val FITS_SMALLEST_CHILD = 0
        private val FITS_LARGEST_CHILD = 1

        /**
         * The direction of rotation, 1 for counter-clockwise, -1 for clockwise
         */
        private val COUNTER_CLOCKWISE = 1
        private val CLOCKWISE = -1

        private val ANIMATION_DURATION: Long = 2000
    }
}