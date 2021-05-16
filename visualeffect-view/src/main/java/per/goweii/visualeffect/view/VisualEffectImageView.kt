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
import androidx.appcompat.widget.AppCompatImageView
import per.goweii.visualeffect.core.VisualEffect
import java.text.NumberFormat
import kotlin.math.max

class VisualEffectImageView : AppCompatImageView {
    private val bitmapCanvas = Canvas()
    private var cacheBitmap: Bitmap? = null

    private val srcRect = Rect()
    private val dstRect = Rect()

    private var renderStartTime = 0L
    private var renderEndTime = 0L

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
    var isShowDebugInfo = BuildConfig.DEBUG
        set(value) {
            if (field != value) {
                field = value
                postInvalidate()
            }
        }
    private val paint = Paint().apply {
        isAntiAlias = true
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

    override fun onDetachedFromWindow() {
        visualEffect?.recycle()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val visualEffect = visualEffect
            ?: kotlin.run {
                super.onDraw(canvas)
                return
            }
        prepare()
        val cacheBitmap = cacheBitmap ?: return
        renderStartTime = System.nanoTime()
        val restoreCount = bitmapCanvas.save()
        bitmapCanvas.drawColor(Color.TRANSPARENT)
        bitmapCanvas.scale(
            cacheBitmap.width.toFloat() / this.width.toFloat(),
            cacheBitmap.height.toFloat() / this.height.toFloat()
        )
        super.onDraw(bitmapCanvas)
        bitmapCanvas.restoreToCount(restoreCount)
        visualEffect.process(cacheBitmap, cacheBitmap)
        renderEndTime = System.nanoTime()
        onDrawEffectedBitmap(canvas, cacheBitmap)
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

    private fun onDrawEffectedBitmap(canvas: Canvas, bitmap: Bitmap) {
        paint.color = Color.WHITE
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
}