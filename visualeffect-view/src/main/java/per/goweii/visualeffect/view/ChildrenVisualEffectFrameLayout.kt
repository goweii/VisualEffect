package per.goweii.visualeffect.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout
import per.goweii.visualeffect.core.VisualEffect

class ChildrenVisualEffectFrameLayout : FrameLayout {
    private val visualEffectHelper = VisualEffectHelper(this)
        .apply { onCallSuperDraw = { super.draw(it) } }

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
}