package sashiro.com.trimmingview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import sashiro.com.trimmingview.ext.*

class TrimmingView(context: Context, attributeSet: AttributeSet?) : DragView(context, attributeSet) {
    constructor(context: Context) : this(context, null)

    // private filed
    private val trimBorderPaint = Paint()
    private val trimBgPaint = Paint()
    private val trimBorderPath = Path()
    private val trimBgPath = Path()

    private var needCalTriRectF = false
    // attrs
    private var trimBorderColor: Int = getColor(R.color.black)
    private var trimBgColor: Int = getColor(R.color.trans_black)
    private var trimBorderWidth: Float = 0f
    private var showTrimBg: Boolean = false
    private var minPadding: Float = 0f

    override fun onGlobalLayout() {
        if (needCalTriRectF) {
            if (centerPointF.isEmpty)
                setCenter(widthF / 2, heightF / 2)

            // calculate trimPath
            // find standard line
            val isWidthStandard = when (rectFHasRotated) {
                false -> ((widthF - 2 * minPadding) / borderRatio) <= heightF
                true -> ((widthF - 2 * minPadding) * borderRatio) <= heightF
            }
            val borderStandardLine = when {
                (isWidthStandard && !rectFHasRotated) ||
                        (isWidthStandard && rectFHasRotated) -> widthF - 2 * minPadding
                else -> heightF - 2 * minPadding
            }
            val otherLine = when {
                (isWidthStandard && !rectFHasRotated) ||
                        (!isWidthStandard && rectFHasRotated) -> borderStandardLine / borderRatio
                else -> borderStandardLine * borderRatio
            }
            val left = when {
                isWidthStandard -> minPadding
                else -> (widthF - otherLine) / 2
            }
            val right = when {
                isWidthStandard -> borderStandardLine + minPadding
                else -> left + otherLine
            }
            val top = when {
                isWidthStandard -> (heightF - otherLine) / 2
                else -> minPadding
            }
            val bottom = when {
                isWidthStandard -> top + otherLine
                else -> borderStandardLine + minPadding
            }
            // trimPath
            trimBorderPath
            trimBorderPath.apply {
                reset()
                moveTo(left, top)
                lineTo(right, top)
                lineTo(right, bottom)
                lineTo(left, bottom)
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
            // setStandardRectF
            standardRectF.apply {
                this.left = left
                this.right = right
                this.top = top
                this.bottom = bottom
            }

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
            color = trimBorderColor
            style = Paint.Style.STROKE
            strokeWidth = trimBorderWidth
        }
        // bg paint
        trimBgPaint.apply {
            isAntiAlias = true
            color = trimBgColor
            style = Paint.Style.FILL_AND_STROKE
        }
    }

    // override method
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (showTrimBg)
            canvas.drawPath(trimBgPath, trimBgPaint)
        canvas.drawPath(trimBorderPath, trimBorderPaint)
    }

    override fun setImageDrawable(drawable: Drawable?) {
        needCalTriRectF = true
        super.setImageDrawable(drawable)
    }

    // private method
    private fun initAttrs(attributeSet: AttributeSet?) {
        attributeSet?.let {
            val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TrimmingView)
            try {
                trimBorderColor = typedArray.getColor(R.styleable.TrimmingView_trimBorderColor, getColor(R.color.black))
                trimBgColor = typedArray.getColor(R.styleable.TrimmingView_trimBgColor, getColor(R.color.trans_black))
                showTrimBg = typedArray.getBoolean(R.styleable.TrimmingView_showTrimBg, false)
                minPadding = typedArray.getDimensionPixelSize(R.styleable.TrimmingView_minPadding, dp2px(10f)).toFloat()
                trimBorderWidth = typedArray.getDimensionPixelSize(R.styleable.TrimmingView_trimBorderWidth, dp2px(1f)).toFloat()
                borderRatio = typedArray.getFloat(R.styleable.TrimmingView_ratio, 1f)
            } finally {
                typedArray.recycle()
            }
        }
    }

    // public method
    fun setRatio(ratio: Float): TrimmingView {
        borderRatio = ratio
        return this
    }

    fun addImgSize(width: Float, height: Float): TrimmingView {
        imgWidth = width
        imgHeight = height
        return this
    }

    fun init() {
        needCalTriRectF = true
        needCalImg = true
        requestLayout()
    }

    fun turnClockwise() =
            rotateImg(getCurrentAngle() + 90f)


    fun turnAnticlockwise() =
            rotateImg(getCurrentAngle() - 90f)

    override fun rotateImg(angle: Float): Float {
        // rotate trimPath
        rectFHasRotated = angle % 180 != 0f
        needCalTriRectF = true
        needCalImg = true
        return super.rotateImg(angle)
    }
}