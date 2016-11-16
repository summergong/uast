fun foo(): Boolean {
    class Local
    fun bar() = Local()

    return bar() == Local()
}