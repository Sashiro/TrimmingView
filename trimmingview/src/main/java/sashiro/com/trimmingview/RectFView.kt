package sashiro.com.trimmingview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import sashiro.com.trimmingview.ext.*
import sashiro.com.trimmingview.model.DragMode

/** @hide */
abstract class RectFView(context: Context, attributeSet: AttributeSet?) : DragView(context, attributeSet) {
    constructor(context: Context) : this(context, null)

    // private filed
    private val trimBorderPaint = Paint()
    private val trimBgPaint = Paint()
    private val trimBorderPath = Path()
    private val trimBgPath = Path()

    protected var needCalTriRectF = false

    override fun onGlobalLayout() {
        if (needCalTriRectF) {
            if (centerPointF.isEmpty)
                setCenter(widthF / 2, heightF / 2)

            calStandardRectF()
            setPathByRectF(standardRectF)

            needCalTriRectF = false
        }

        // init Img
        super.onGlobalLayout()
    }

    init {
        initAttrs(attributeSet)

        // init paint
        // border paint
        trimBorderPaint.apply {
            isAntiAlias = true
            color = config.borderColor
            style = Paint.Style.STROKE
            strokeWidth = config.borderWidth
        }
        // bg paint
        trimBgPaint.apply {
            isAntiAlias = true
            color = config.backgroundColor
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    // override method
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (config.showBackground)
            canvas.drawPath(trimBgPath, trimBgPaint)
        canvas.drawPath(trimBorderPath, trimBorderPaint)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        needCalTriRectF = true
        super.setImageDrawable(drawable)
    }

    
    // protected method
    protected fun applyColor() {
        trimBorderPaint.apply {
            color = config.borderColor
            strokeWidth = config.borderWidth
        }
        // bg paint
        trimBgPaint.apply {
            color = config.backgroundColor
        }
    }

    protected fun calStandardRectF() {
        // find standard line
        val isWidthStandard = when (rectFHasRotated) {
            false -> ((widthF - 2 * config.minPadding) / config.ratio) <= heightF
            true -> ((widthF - 2 * config.minPadding) * config.ratio) <= heightF
        }
        val borderStandardLine = when {
            (isWidthStandard && !rectFHasRotated) ||
                    (isWidthStandard && rectFHasRotated) -> widthF - 2 * config.minPadding
            else -> heightF - 2 * config.minPadding
        }
        val otherLine = when {
            (isWidthStandard && !rectFHasRotated) ||
                    (!isWidthStandard && rectFHasRotated) -> borderStandardLine / config.ratio
            else -> borderStandardLine * config.ratio
        }
        val left = when {
            isWidthStandard -> config.minPadding
            else -> (widthF - otherLine) / 2
        }
        val right = when {
            isWidthStandard -> borderStandardLine + config.minPadding
            else -> left + otherLine
        }
        val top = when {
            isWidthStandard -> (heightF - otherLine) / 2
            else -> config.minPadding
        }
        val bottom = when {
            isWidthStandard -> top + otherLine
            else -> borderStandardLine + config.minPadding
        }
        standardRectF.apply {
            this.left = left
            this.right = right
            this.top = top
            this.bottom = bottom
        }
    }

    protected fun setPathByRectF(rectF: RectF) {
        val squarePoint = SquarePoint(
                PointF(rectF.left, rectF.top),
                PointF(rectF.right, rectF.top),
                PointF(rectF.right, rectF.bottom),
                PointF(rectF.left, rectF.bottom))
        setPathBySquarePoint(squarePoint)
    }

    protected fun setPathBySquarePoint(squarePoint: SquarePoint) {
        trimBorderPath.apply {
            reset()
            moveTo(squarePoint.ltPoint.x, squarePoint.ltPoint.y)
            lineTo(squarePoint.rtPoint.x, squarePoint.rtPoint.y)
            lineTo(squarePoint.rbPoint.x, squarePoint.rbPoint.y)
            lineTo(squarePoint.lbPoint.x, squarePoint.lbPoint.y)
            close()
        }
        trimBgPath.apply {
            reset()
            moveTo(0f, 0f)
            lineTo(widthF, 0f)
            lineTo(widthF, heightF)
            lineTo(0f, heightF)
            close()
            fillType = Path.FillType.EVEN_ODD
            addPath(trimBorderPath)
        }
    }

    // private method
    private fun initAttrs(attributeSet: AttributeSet?) {
        attributeSet?.let {
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TrimmingView)
            try {
                (0..typedArray.indexCount).map { i ->
                    typedArray.getIndex(i)
                }.forEach { index ->
                    when (index) {
                        R.styleable.TrimmingView_trimBorderColor -> config.borderColor = typedArray.getColor(index, getColor(R.color.black))
                        R.styleable.TrimmingView_trimBgColor -> config.backgroundColor = typedArray.getColor(index, getColor(R.color.trans_black))
                        R.styleable.TrimmingView_showTrimBg -> config.showBackground = typedArray.getBoolean(index, false)
                        R.styleable.TrimmingView_minPadding -> config.minPadding = typedArray.getDimensionPixelSize(index, resources.getDimensionPixelSize(R.dimen.min_padding_default)).toFloat()
                        R.styleable.TrimmingView_trimBorderWidth -> config.borderWidth = typedArray.getDimensionPixelSize(index, resources.getDimensionPixelSize(R.dimen.border_width_default)).toFloat()
                        R.styleable.TrimmingView_ratio -> config.ratio = typedArray.getFloat(index, 1f)
                        R.styleable.TrimmingView_dragMode -> config.dragMode = when (typedArray.getInt(index, 0)) {
                            1 -> DragMode.OverDrag
                            2 -> DragMode.Disabled
                            else -> DragMode.Default
                        }
                    }
                }
            } finally {
                typedArray.recycle()
            }
        }
    }

    // inner class
    protected data class SquarePoint(
            val ltPoint: PointF = PointF(),
            val rtPoint: PointF = PointF(),
            val rbPoint: PointF = PointF(),
            val lbPoint: PointF = PointF()
    ) {
        companion object {
            fun transForm(rectF: RectF): SquarePoint =
                    SquarePoint(
                            PointF(rectF.left, rectF.top),
                            PointF(rectF.right, rectF.top),
                            PointF(rectF.right, rectF.bottom),
                            PointF(rectF.left, rectF.bottom))
        }
    }


}