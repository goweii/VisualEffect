package per.goweii.visualeffect.core

import android.graphics.Bitmap

interface VisualEffect {
    fun process(input: Bitmap, output: Bitmap)
    fun recycle()
}