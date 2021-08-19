package per.goweii.visualeffect.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import per.goweii.visualeffect.core.VisualEffect

open class ChildrenVisualEffectFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val visualEffectHelper by lazy {
        ChildrenVisualEffectHelper(this)
    }

    var visualEffect: VisualEffect?
        get() = visualEffectHelper.visualEffect
        set(value) {
            visualEffectHelper.visualEffect = value
        }
    var simpleSize: Float
        get() = visualEffectHelper.simpleSize
        set(value) {
            visualEffectHelper.simpleSize = value
        }
    var isShowDebugInfo: Boolean
        get() = visualEffectHelper.isShowDebugInfo
        set(value) {
            visualEffectHelper.isShowDebugInfo = value
        }
    val isRendering get() = visualEffectHelper.isRendering

    override fun draw(canvas: Canvas) {
        visualEffectHelper.draw(canvas) {
            super.draw(it)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        visualEffectHelper.onRestoreInstanceState(state) {
            super.onRestoreInstanceState(it)
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return visualEffectHelper.onSaveInstanceState {
            super.onSaveInstanceState()
        }
    }
}