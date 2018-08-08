package sashiro.com.trimmingview

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewTreeObserver
import sashiro.com.trimmingview.model.TriResult


open class DragView(context: Context, attributeSet: AttributeSet?) : AppCompatImageView(context, attributeSet),
        ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener {

    constructor(context: Context) : this(context, null)

    // private field
    private var maxScale = 1f
    private var minScale = 1f
    private var lastPointerCount = 0
    private val gestureDetector = ScaleGestureDetector(context, this)
    private val photoMatrix = Matrix()
    private val matrixValues = FloatArray(9)
    private val lastTouchPointF = PointF()

    // protected field
    protected var needCalImg = true
    protected val standardRectF = RectF()
    protected val centerPointF = PointF()
    protected val dragInfo = 1
    protected val lastTriResult = TriResult()

    // onGlobalLayout
    override fun onGlobalLayout() {

    }

    // init
    init {
        attributeSet?.let {

        }
    }

    // onScale
    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {

    }

    override fun onScale(p0: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        return true
    }

    // private method
}