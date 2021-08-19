package per.goweii.visualeffect.view

import android.graphics.*
import android.view.View

class OutlineHelper(private val view: View) {
    private var outlinePath: Path? = null
    private var outlineInvalidate = true
    private var outlinePaint: Paint? = null
    private var outlineXfermode: PorterDuffXfermode? = null

    var outlineBuilder: OutlineBuilder? = null
        set(value) {
            if (value == null) {
                if (field != null) {
                    field?.detachFromVisualEffectOutlineHelper()
                    field = null
                    invalidateOutline()
                }
            } else {
                if (field !== value) {
                    field = value
                    field?.attachToVisualEffectOutlineHelper(this)
                    invalidateOutline()
                }
            }
        }

    val outline: Path?
        get() {
            if (outlineInvalidate) {
                rebuildOutline()
            }
            return outlinePath
        }

    val suggestedMinimumWidth: Int
        get() = outlineBuilder?.getSuggestedMinimumWidth() ?: 0

    val suggestedMinimumHeight: Int
        get() = outlineBuilder?.getSuggestedMinimumHeight() ?: 0

    fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        invalidateOutline()
    }

    fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        invalidateOutline()
    }

    fun draw(canvas: Canvas, callSuper: (Canvas) -> Unit) {
        val path = outline
        if (path == null) {
            outlinePaint = null
            outlineXfermode = null
            callSuper.invoke(canvas)
            return
        }
        val paint = outlinePaint ?: Paint(Paint.ANTI_ALIAS_FLAG).also {
            outlinePaint = it
        }
        val xfermode = outlineXfermode ?: PorterDuffXfermode(PorterDuff.Mode.DST_OUT).also {
            outlineXfermode = it
        }
        val layerId = canvas.saveLayer(
            0f,
            0f,
            view.width.toFloat(),
            view.height.toFloat(),
            null,
            Canvas.ALL_SAVE_FLAG
        )
        callSuper.invoke(canvas)
        path.toggleInverseFillType()
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        paint.xfermode = xfermode
        canvas.drawPath(path, paint)
        paint.xfermode = null
        path.toggleInverseFillType()
        canvas.restoreToCount(layerId)
    }

    fun invalidateOutline() {
        outlineInvalidate = true
        view.invalidate()
    }

    private fun rebuildOutline() {
        if (outlineBuilder == null) {
            outlinePath = null
        } else {
            val path = outlinePath?.also {
                it.reset()
                it.rewind()
            } ?: Path().also {
                outlinePath = it
            }
            outlineBuilder!!.buildOutline(view, path)
            if (path.isEmpty) {
                path.close()
            }
        }
    }
}