package per.goweii.visualeffect.view

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.getActivity(): Activity? {
    var context = this
    while (true) {
        if (context is Activity) {
            return context
        }
        if (context is ContextWrapper) {
            val baseContext = context.baseContext
            if (baseContext !== context) {
                context = baseContext
                continue
            }
        }
        return null
    }
}