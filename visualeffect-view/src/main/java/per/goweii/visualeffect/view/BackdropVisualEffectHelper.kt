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
    private var bitmapCanvas: Canvas? = null
    private var cacheBitmap: Bitmap? = null
    private var activityDecorView: View? = null
    private var isDifferentRoot = false

    private val renderingListener = RenderingListener()
    private val locations = IntArray(2)
    private val srcRect = Rect()
    private val dstRect = Rect()

    private val paint = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.MONOSPACE
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            9F,
            view.context.resources.displayMetrics
        )
    }

    private val realScaleXY: FloatArray = floatArrayOf(1F, 1F)
        get() {
            field[0] = view.scaleX
            field[1] = view.scaleY
            var viewGroup: ViewGroup? = view.parent as? ViewGroup?
            while (viewGroup != null) {
                field[0] *= viewGroup.scaleX
                field[1] *= viewGroup.scaleY
                viewGroup = viewGroup.parent as? ViewGroup?
            }
            return field
        }

    private var renderStartTime = 0L
    private var renderEndTime = 0L

    val isRendering: Boolean get() = renderEndTime < renderStartTime

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

    fun checkRendering() {
        if (isRendering) {
            throw StopException
        }
    }

    fun onDraw(canvas: Canvas) {
        checkRendering()
        cacheBitmap?.let {
            onDrawEffectedBitmap(canvas, it)
        }
        canvas.drawColor(overlayColor)
        if (isShowDebugInfo) {
            onDrawDebugInfo(canvas)
        }
    }

    fun onRestoreInstanceState(state: Parcelable?, callSuper: (Parcelable?) -> Unit) {
        if (state !is SavedState) {
            callSuper.invoke(state)
            return
        }
        callSuper.invoke(state.superState)
        isShowDebugInfo = state.isShowDebugInfo
        simpleSize = state.simpleSize
        overlayColor = state.overlayColor
        visualEffect = state.visualEffect
    }

    fun onSaveInstanceState(callSuper: () -> Parcelable?): Parcelable {
        val superState = callSuper.invoke() ?: View.BaseSavedState.EMPTY_STATE
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
        registerRenderingListener()
        activityDecorView?.let {
            isDifferentRoot = it.rootView !== view.rootView
            if (isDifferentRoot) {
                it.postInvalidate()
            }
        } ?: kotlin.run {
            isDifferentRoot = false
        }
    }

    private fun onDetachedFromWindow() {
        unregisterRenderingListener()
        visualEffect?.recycle()
    }

    private fun registerRenderingListener() {
        activityDecorView?.let {
            if (it.viewTreeObserver.isAlive) {
                it.viewTreeObserver.addOnPreDrawListener(renderingListener)
            }
            isDifferentRoot = it.rootView !== view.rootView
            if (isDifferentRoot) {
                it.postInvalidate()
            }
        }
    }

    private fun unregisterRenderingListener() {
        activityDecorView?.let {
            if (it.viewTreeObserver.isAlive) {
                it.viewTreeObserver.removeOnPreDrawListener(renderingListener)
            }
        }
    }

    private fun prepare() {
        val simpledWidth = (view.width / simpleSize).toInt()
        val simpledHeight = (view.height / simpleSize).toInt()
        if (simpledWidth <= 0 || simpledHeight <= 0) {
            bitmapCanvas = null
            cacheBitmap = null
        } else if (cacheBitmap == null || cacheBitmap!!.width != simpledWidth || cacheBitmap!!.height != simpledHeight) {
            cacheBitmap = try {
                Bitmap.createBitmap(simpledWidth, simpledHeight, Bitmap.Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                Runtime.getRuntime().gc()
                null
            }
            if (cacheBitmap != null) {
                if (bitmapCanvas == null) {
                    bitmapCanvas = Canvas()
                }
                bitmapCanvas!!.setBitmap(cacheBitmap)
            } else {
                bitmapCanvas = null
            }
        }
    }

    private fun renderOnce() {
        val visualEffect = visualEffect ?: return
        if (!view.isShown) return
        val decor = activityDecorView ?: return
        if (!decor.isDirty) return
        prepare()
        val canvas = bitmapCanvas ?: return
        val bitmap = cacheBitmap ?: return
        renderStartTime = System.nanoTime()
        bitmap.eraseColor(Color.TRANSPARENT)
        captureToBitmap(decor, canvas, bitmap)
        visualEffect.process(bitmap, bitmap)
        renderEndTime = System.nanoTime()
        view.invalidate()
    }

    private fun captureToBitmap(decor: View, canvas: Canvas, bitmap: Bitmap) {
        val restoreCount = canvas.save()
        try {
            decor.getLocationOnScreen(locations)
            var x = -locations[0]
            var y = -locations[1]
            view.getLocationOnScreen(locations)
            x += locations[0]
            y += locations[1]
            val realScaleXX = realScaleXY
            val vw = view.width.toFloat() * realScaleXX[0]
            val vh = view.height.toFloat() * realScaleXX[1]
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

    private inner class RenderingListener : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            renderOnce()
            return true
        }
    }

    private object StopException : RuntimeException()
}