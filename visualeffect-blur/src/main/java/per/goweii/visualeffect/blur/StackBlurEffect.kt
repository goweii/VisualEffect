package per.goweii.visualeffect.blur

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.*
import android.util.Log
import per.goweii.visualeffect.blur.rs.ScriptC_StackBlur

class StackBlurEffect @JvmOverloads constructor(
    context: Context,
    radius: Float = 8F
) : BlurEffect(radius) {
    private val tag = this::class.java.simpleName
    private val applicationContext: Context = context.applicationContext

    private var renderScript: RenderScript? = null
    private var blurScript: ScriptC_StackBlur? = null
    private var allocationIn: Allocation? = null
    private var allocationOut: Allocation? = null
    private var bitmapArgb8888: Bitmap? = null

    override fun recycle() {
        super.recycle()
        destroyAllocations()
        destroyScripts()
        bitmapArgb8888?.recycle()
        bitmapArgb8888 = null
    }

    override fun doEffect(input: Bitmap, output: Bitmap) {
        check(input.width == output.width && input.height == output.height)
        val radius = radius.toInt()
        if (radius == 0 && input === output) return
        prepareScripts()
        val blurScript = blurScript ?: return
        val inputBitmap = getBitmapArgb8888(input)
        prepareAllocations(inputBitmap)
        val allocInput = allocationIn ?: return
        val allocOutput = allocationOut ?: return
        if (radius == 0) {
            allocInput.copyTo(output)
            return
        }

        blurScript._width = input.width
        blurScript._height = input.height
        blurScript._radius = radius

        blurScript._input = allocInput
        blurScript._output = allocOutput
        blurScript.forEach_stackblur_v(allocInput)

        blurScript._input = allocOutput
        blurScript._output = allocInput
        blurScript.forEach_stackblur_h(allocOutput)

        val outputBitmap = getBitmapArgb8888(output)
        allocInput.copyTo(outputBitmap)
        if (outputBitmap != output) {
            copyBitmap(outputBitmap, output, false)
        }
    }

    private fun getBitmapArgb8888(bitmap: Bitmap): Bitmap {
        if (bitmap.config == Bitmap.Config.ARGB_8888) {
            return bitmap
        }
        Log.w(tag, "Bitmap config should be ARGB_8888")
        bitmapArgb8888?.let {
            if (it.width != bitmap.width || it.height != bitmap.height) {
                it.recycle()
                bitmapArgb8888 = null
            }
        }
        val newBitmap = Bitmap.createBitmap(
            bitmap.width,
            bitmap.height,
            Bitmap.Config.ARGB_8888
        ).also {
            bitmapArgb8888 = it
        }
        copyBitmap(bitmap, newBitmap, false)
        return newBitmap
    }

    private fun prepareScripts() {
        if (renderScript == null) {
            renderScript = RenderScript.create(applicationContext)
        }
        if (blurScript == null) {
            blurScript = ScriptC_StackBlur(renderScript)
        }
    }

    private fun prepareAllocations(bitmap: Bitmap) {
        if (allocationIn == null || allocationOut == null) {
            destroyAllocations()
            createAllocations(bitmap)
            return
        }
        if (allocationIn!!.type.x != bitmap.width || allocationIn!!.type.y != bitmap.height) {
            destroyAllocations()
            createAllocations(bitmap)
            return
        }
        try {
            allocationIn!!.copyFrom(bitmap)
        } catch (ignore: RSIllegalArgumentException) {
            destroyAllocations()
            createAllocations(bitmap)
        }
    }

    private fun createAllocations(bitmap: Bitmap) {
        allocationIn = Allocation.createFromBitmap(
            renderScript,
            bitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
        allocationOut = Allocation.createTyped(
            renderScript,
            allocationIn!!.type
        )
    }

    private fun destroyAllocations() {
        try {
            allocationIn?.destroy()
            allocationIn = null
        } catch (ignore: Exception) {
        }
        try {
            allocationOut?.destroy()
            allocationOut = null
        } catch (ignore: Exception) {
        }
    }

    private fun destroyScripts() {
        try {
            renderScript?.destroy()
            renderScript = null
        } catch (ignore: Exception) {
        }
        try {
            blurScript?.destroy()
            blurScript = null
        } catch (ignore: Exception) {
        }
    }
}