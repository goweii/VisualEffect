package per.goweii.android.visualeffect

inline fun timeCost(block: () -> Unit) : Long {
    val start = System.currentTimeMillis()
    block()
    val end = System.currentTimeMillis()
    return end - start
}