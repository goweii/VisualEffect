package per.goweii.visualeffect.blur

import android.os.Parcel
import per.goweii.visualeffect.core.BaseVisualEffect

abstract class BlurEffect constructor(var radius: Float) : BaseVisualEffect() {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeFloat(radius)
    }

    override fun readFromParcel(parcel: Parcel) {
        super.readFromParcel(parcel)
        radius = parcel.readFloat()
    }
}