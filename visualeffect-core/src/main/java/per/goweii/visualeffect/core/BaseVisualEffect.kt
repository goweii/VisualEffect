package per.goweii.visualeffect.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter

abstract class BaseVisualEffect : VisualEffect {
    private val canvas = Canvas()
    private val paint = Paint()
    private val antiAliasDrawFilter =
        PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    override fun process(input: Bitmap, output: Bitmap) {
        if (input === output) {
            doEffect(input, output)
        } else {
            if (input.width != output.width || input.height != output.height) {
                copyBitmap(input, output, false)
                doEffect(output, output)
            } else {
                doEffect(input, output)
            }
        }
    }

    override fun recycle() {
        canvas.drawFilter = null
        canvas.setBitmap(null)
        paint.reset()
    }

    protected abstract fun doEffect(input: Bitmap, output: Bitmap)

    @Suppress("SameParameterValue")
    protected fun copyBitmap(input: Bitmap, output: Bitmap, antiAlias: Boolean) {
        useCanvas(output, antiAlias) { canvas, paint ->
            canvas.scale(
                output.width.toFloat() / input.width.toFloat(),
                output.height.toFloat() / input.height.toFloat()
            )
            canvas.drawBitmap(input, 0F, 0F, paint)
        }
    }

    protected fun useCanvas(
        bitmap: Bitmap,
        isAntiAlias: Boolean = false,
        action: (canvas: Canvas, paint: Paint) -> Unit
    ) {
        val canvas = this.canvas
        val paint = this.paint
        if (isAntiAlias) {
            canvas.drawFilter = antiAliasDrawFilter
            paint.isAntiAlias = true
        } else {
            canvas.drawFilter = null
            paint.isAntiAlias = false
        }
        canvas.setBitmap(bitmap)
        val saveCount = canvas.save()
        action(canvas, paint)
        canvas.restoreToCount(saveCount)
        canvas.setBitmap(null)
        canvas.drawFilter = null
        paint.isAntiAlias = false
    }
}