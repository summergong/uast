package org.jetbrains.uast.values

// Something with value that cannot be evaluated
object UUndeterminedValue : UValueBase() {
    override fun toString() = "Undetermined"
}

fun UValue.ifUndetermined(block: () -> UValue) = if (this == UUndeterminedValue) block() else this
