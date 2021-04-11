package per.goweii.visualeffect.core

import android.graphics.Bitmap

class GroupVisualEffect(vararg visualEffects: VisualEffect) : BaseVisualEffect() {
    private val visualEffects = arrayListOf<VisualEffect>()

    init {
        this.visualEffects.addAll(visualEffects)
    }

    fun getVisualEffects(): List<VisualEffect> {
        return visualEffects
    }

    fun addVisualEffect(visualEffect: VisualEffect) {
        visualEffects.add(visualEffect)
    }

    fun removeVisualEffect(visualEffect: VisualEffect) {
        visualEffects.remove(visualEffect)
    }

    override fun doEffect(input: Bitmap, output: Bitmap) {
        visualEffects.forEachIndexed { index, visualEffect ->
            if (index == 0) {
                visualEffect.process(input, output)
            } else {
                visualEffect.process(output, output)
            }
        }
    }

    override fun recycle() {
        super.recycle()
        visualEffects.forEach {
            it.recycle()
        }
    }
}