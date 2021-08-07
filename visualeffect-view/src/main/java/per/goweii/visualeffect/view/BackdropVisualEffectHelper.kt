package per.goweii.visualeffect.view

import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import per.goweii.visualeffect.core.ParcelableVisualEffect
import per.goweii.visualeffect.core.VisualEffect
import java.text.NumberFormat
import kotlin.math.max

class BackdropVisualEffectHelper(private val view: View) {
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
    private val isRendering: Boolean get() = renderEndTime < renderStartTime

    var overlayColor: Int = Color.TRANSPARENT
        set(value) {
            if (field != value) {
                field = value
                view.postInvalidate()
            }
        }
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

    private val paint = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.MONOSPACE
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            9F,
            view.context.resources.displayMetrics
        )
    }

    private val realScaleX: Float
        get() {
            var realScaleX = view.scaleX
            var viewGroup: ViewGroup? = view.parent as? ViewGroup?
            while (viewGroup != null) {
                realScaleX *= viewGroup.scaleX
                viewGroup = viewGroup.parent as? ViewGroup?
            }
            return realScaleX
        }

    private val realScaleY: Float
        get() {
            var realScaleY = view.scaleY
            var viewGroup: ViewGroup? = view.parent as? ViewGroup?
            while (viewGroup != null) {
                realScaleY *= viewGroup.scaleY
                viewGroup = viewGroup.parent as? ViewGroup?
            }
            return realScaleY
        }

    var onCallSuperRestoreInstanceState: ((state: Parcelable?) -> Unit)? = null
    var onCallSuperSaveInstanceState: (() -> Parcelable?)? = null

    init {
        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                onAttachedToWindow()
            }

            override fun onViewDetachedFromWindow(v: View) {
                onDetachedFromWindow()
            }
        })
    }

    fun onDraw(canvas: Canvas) {
        if (isRendering) {
            throw StopException
        } else {
            cacheBitmap?.let {
                onDrawEffectedBitmap(canvas, it)
            }
            if (isShowDebugInfo) {
                onDrawDebugInfo(canvas)
            }
        }
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            onCallSuperRestoreInstanceState?.invoke(state)
            return
        }
        onCallSuperRestoreInstanceState?.invoke(state.superState)
        isShowDebugInfo = state.isShowDebugInfo
        simpleSize = state.simpleSize
        overlayColor = state.overlayColor
        visualEffect = state.visualEffect
    }

    fun onSaveInstanceState(): Parcelable {
        val superState = onCallSuperSaveInstanceState?.invoke() ?: View.BaseSavedState.EMPTY_STATE
        return SavedState(
            superState = superState,
            isShowDebugInfo = isShowDebugInfo,
            simpleSize = simpleSize,
            overlayColor = overlayColor,
            visualEffect = visualEffect as? ParcelableVisualEffect?
        )
    }

    private fun onAttachedToWindow() {
        view.context.getActivity()?.let {
            activityDecorView = it.window?.decorView
        }
        activityDecorView?.let {
            if (it.viewTreeObserver.isAlive) {
                it.viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
            }
            isDifferentRoot = it.rootView !== view.rootView
            if (isDifferentRoot) {
                it.postInvalidate()
            }
        } ?: kotlin.run {
            isDifferentRoot = false
        }
    }

    private fun onDetachedFromWindow() {
        activityDecorView?.let {
            if (it.viewTreeObserver.isAlive) {
                it.viewTreeObserver.removeOnPreDrawListener(onPreDrawListener)
            }
        }
        visualEffect?.recycle()
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

    private fun renderOnce() {
        val visualEffect = visualEffect ?: return
        if (!view.isShown) return
        val decor = activityDecorView ?: return
        if (!decor.isDirty) return
        prepare()
        val bitmap = cacheBitmap ?: return
        val canvas = bitmapCanvas
        renderStartTime = System.nanoTime()
        captureToBitmap(decor, canvas, bitmap)
        visualEffect.process(bitmap, bitmap)
        renderEndTime = System.nanoTime()
        view.invalidate()
    }

    private fun captureToBitmap(decor: View, canvas: Canvas, bitmap: Bitmap) {
        val restoreCount = canvas.save()
        try {
            decor.getLocationInWindow(locations)
            var x = -locations[0]
            var y = -locations[1]
            view.getLocationInWindow(locations)
            x += locations[0]
            y += locations[1]
            val vw = view.width.toFloat() * realScaleX
            val vh = view.height.toFloat() * realScaleY
            canvas.scale(
                bitmap.width.toFloat() / vw,
                bitmap.height.toFloat() / vh
            )
            canvas.translate(-x.toFloat(), -y.toFloat())
            decor.background?.draw(canvas)
            decor.draw(canvas)
        } catch (e: StopException) {
        } finally {
            canvas.restoreToCount(restoreCount)
        }
    }

    private fun onDrawEffectedBitmap(canvas: Canvas, bitmap: Bitmap) {
        paint.color = Color.WHITE
        srcRect.right = bitmap.width
        srcRect.bottom = bitmap.height
        dstRect.right = view.width
        dstRect.bottom = view.height
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint)
        canvas.drawColor(overlayColor)
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

    private class SavedState(
        superState: Parcelable,
        val isShowDebugInfo: Boolean,
        val simpleSize: Float,
        val overlayColor: Int,
        val visualEffect: ParcelableVisualEffect?
    ) : View.BaseSavedState(superState) {
        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(if (isShowDebugInfo) 1 else 0)
            dest.writeFloat(simpleSize)
            dest.writeInt(overlayColor)
            dest.writeParcelable(visualEffect, 0)
        }
    }

    private object StopException : RuntimeException()
}