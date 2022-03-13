package per.goweii.visualeffect.simple

inline fun timeCost(block: () -> Unit) : Long {
    val start = System.currentTimeMillis()
    block()
    val end = System.currentTimeMillis()
    return end - start
}