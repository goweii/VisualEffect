package per.goweii.visualeffect.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import per.goweii.visualeffect.core.VisualEffect

class ChildrenVisualEffectFrameLayout : FrameLayout {
    private val visualEffectHelper = ChildrenVisualEffectHelper(this)
        .apply {
            onCallSuperDraw = { super.draw(it) }
            onCallSuperRestoreInstanceState = { super.onRestoreInstanceState(it) }
            onCallSuperSaveInstanceState = { super.onSaveInstanceState() }
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

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) {
        visualEffectHelper.draw(canvas)
    }

    @SuppressLint("MissingSuperCall")
    override fun onRestoreInstanceState(state: Parcelable?) {
        visualEffectHelper.onRestoreInstanceState(state)
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(): Parcelable {
        return visualEffectHelper.onSaveInstanceState()
    }
}