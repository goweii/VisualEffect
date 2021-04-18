package per.goweii.visualeffect.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import per.goweii.visualeffect.core.VisualEffect
import java.text.NumberFormat
import kotlin.math.max

class VisualEffectHelper(private val view: View) {
    private var cacheBitmap: Bitmap? = null
    private val bitmapCanvas = Canvas()
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.TRANSPARENT
        typeface = Typeface.MONOSPACE
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            9F,
            view.context.resources.displayMetrics
        )
    }

    private val srcRect = Rect()
    private val dstRect = Rect()

    private var renderStartTime = 0L
    private var renderEndTime = 0L

    var visualEffect: VisualEffect? = null
        set(value) {
            if (field != value) {
                field?.recycle()
                field = value
                view.postInvalidate()
            }
        }
    var simpleSize = 1F
        set(value) {
            if (field != value) {
                field = max(1F, value)
                view.postInvalidate()
            }
        }
    var isShowDebugInfo = BuildConfig.DEBUG
        set(value) {
            if (field != value) {
                field = value
                view.postInvalidate()
            }
        }

    private val onAttachStateChangeListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View?) {
        }

        override fun onViewDetachedFromWindow(v: View?) {
            visualEffect?.recycle()
        }
    }

    var onCallSuperDraw: ((canvas: Canvas) -> Unit)? = null

    init {
        view.addOnAttachStateChangeListener(onAttachStateChangeListener)
    }

    fun draw(canvas: Canvas) {
        val visualEffect = visualEffect ?: kotlin.run {
            onCallSuperDraw?.invoke(canvas)
            return
        }
        prepare()
        val cacheBitmap = cacheBitmap ?: return
        renderStartTime = System.nanoTime()
        val restoreCount = bitmapCanvas.save()
        bitmapCanvas.drawColor(Color.TRANSPARENT)
        bitmapCanvas.scale(
            cacheBitmap.width.toFloat() / view.width.toFloat(),
            cacheBitmap.height.toFloat() / view.height.toFloat()
        )
        onCallSuperDraw?.invoke(bitmapCanvas)
        bitmapCanvas.restoreToCount(restoreCount)
        visualEffect.process(cacheBitmap, cacheBitmap)
        renderEndTime = System.nanoTime()
        onDrawEffectedBitmap(canvas, cacheBitmap)
        if (isShowDebugInfo) {
            onDrawDebugInfo(canvas)
        }
    }

    private fun prepare() {
        val simpledWidth = (view.width / simpleSize).toInt()
        val simpledHeight = (view.height / simpleSize).toInt()
        if (cacheBitmap == null || cacheBitmap!!.width != simpledWidth || cacheBitmap!!.height != simpledHeight) {
            cacheBitmap = try {
                Bitmap.createBitmap(simpledWidth, simpledHeight, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                null
            }
            bitmapCanvas.setBitmap(cacheBitmap)
        }
    }

    private fun onDrawEffectedBitmap(canvas: Canvas, bitmap: Bitmap) {
        srcRect.right = bitmap.width
        srcRect.bottom = bitmap.height
        dstRect.right = view.width
        dstRect.bottom = view.height
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
            view.width - paint.measureText(costText),
            textBaseLine,
            paint.apply {
                color = if (costTime > 16.6F) Color.RED else Color.BLACK
            }
        )
        val bmpSizeText = "${cacheBitmap?.width ?: 0}*${cacheBitmap?.height ?: 0}"
        textBaseLine += -paint.fontMetrics.ascent
        canvas.drawText(
            bmpSizeText,
            view.width - paint.measureText(bmpSizeText),
            textBaseLine,
            paint.apply {
                color = Color.BLACK
            }
        )
    }
}