package per.goweii.visualeffect.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import per.goweii.visualeffect.core.VisualEffect
import java.text.NumberFormat
import kotlin.math.max

class BackdropVisualEffectView : View {
    private val bitmapCanvas = Canvas()
    private var cacheBitmap: Bitmap? = null
    private var activityDecorView: View? = null
    private var isDifferentRoot = false

    private val srcRect = Rect()
    private val dstRect = Rect()

    private val locations = IntArray(2)
    private val onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
        renderOnce()
        true
    }
    private var renderStartTime = 0L
    private var renderEndTime = 0L
    private val isRendering: Boolean
        get() = renderEndTime < renderStartTime

    var visualEffect: VisualEffect? = null
        set(value) {
            if (field != value) {
                field?.recycle()
                field = value
                postInvalidate()
            }
        }
    var simpleSize = 1F
        set(value) {
            if (field != value) {
                field = max(1F, value)
                postInvalidate()
            }
        }
    var isShowDebugInfo = true
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.TRANSPARENT
        typeface = Typeface.MONOSPACE
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            9F,
            context.resources.displayMetrics
        )
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        context.getActivity()?.let {
            activityDecorView = it.window?.decorView
        }
        activityDecorView?.let {
            if (it.viewTreeObserver.isAlive) {
                it.viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
            }
            isDifferentRoot = it.rootView !== rootView
            if (isDifferentRoot) {
                it.postInvalidate()
            }
        } ?: kotlin.run {
            isDifferentRoot = false
        }
    }

    override fun onDetachedFromWindow() {
        activityDecorView?.let {
            if (it.viewTreeObserver.isAlive) {
                it.viewTreeObserver.removeOnPreDrawListener(onPreDrawListener)
            }
        }
        visualEffect?.recycle()
        super.onDetachedFromWindow()
    }

    override fun draw(canvas: Canvas) {
        if (isRendering) {
            throw StopException
        } else {
            super.draw(canvas)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cacheBitmap?.let {
            onDrawEffectedBitmap(canvas, it)
        }
        if (isShowDebugInfo) {
            onDrawDebugInfo(canvas)
        }
    }

    private fun prepare() {
        val simpledWidth = (width / simpleSize).toInt()
        val simpledHeight = (height / simpleSize).toInt()
        if (cacheBitmap == null || cacheBitmap!!.width != simpledWidth || cacheBitmap!!.height != simpledHeight) {
            cacheBitmap = try {
                Bitmap.createBitmap(simpledWidth, simpledHeight, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                null
            }
            bitmapCanvas.setBitmap(cacheBitmap)
        }
    }

    private fun renderOnce() {
        val visualEffect = visualEffect ?: return
        if (!isShown) return
        val decor = activityDecorView ?: return
        if (!decor.isDirty) return
        prepare()
        val bitmap = cacheBitmap ?: return
        val canvas = bitmapCanvas
        renderStartTime = System.nanoTime()
        captureToBitmap(decor, canvas, bitmap)
        visualEffect.process(bitmap, bitmap)
        renderEndTime = System.nanoTime()
        invalidate()
    }

    private fun captureToBitmap(view: View, canvas: Canvas, bitmap: Bitmap) {
        val restoreCount = canvas.save()
        try {
            canvas.drawColor(Color.WHITE)
            view.getLocationOnScreen(locations)
            var x = -locations[0]
            var y = -locations[1]
            this.getLocationOnScreen(locations)
            x += locations[0]
            y += locations[1]
            canvas.scale(
                bitmap.width.toFloat() / this.width.toFloat(),
                bitmap.height.toFloat() / this.height.toFloat()
            )
            canvas.translate(-x.toFloat(), -y.toFloat())
            view.background?.draw(canvas)
            view.draw(canvas)
        } catch (e: StopException) {
        } finally {
            canvas.restoreToCount(restoreCount)
        }
    }

    private fun onDrawEffectedBitmap(canvas: Canvas, bitmap: Bitmap) {
        srcRect.right = bitmap.width
        srcRect.bottom = bitmap.height
        dstRect.right = width
        dstRect.bottom = height
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
    }

    private fun onDrawDebugInfo(canvas: Canvas) {
        var textBaseLine = 0F
        val costTime = (renderEndTime - renderStartTime).toDouble() / 1_000_000
        val costText = NumberFormat.getInstance().let {
            it.isGroupingUsed = false
            it.minimumFractionDigits = 3
            it.maximumFractionDigits = 3
            it.format(costTime)
        }
        textBaseLine += -paint.fontMetrics.ascent
        canvas.drawText(
            costText,
            width - paint.measureText(costText),
            textBaseLine,
            paint.apply {
                color = if (costTime > 16.6F) Color.RED else Color.BLACK
            }
        )
        val bmpSizeText = "${cacheBitmap?.width ?: 0}*${cacheBitmap?.height ?: 0}"
        textBaseLine += -paint.fontMetrics.ascent
        canvas.drawText(
            bmpSizeText,
            width - paint.measureText(bmpSizeText),
            textBaseLine,
            paint.apply {
                color = Color.BLACK
            }
        )
    }

    private object StopException : RuntimeException()
}