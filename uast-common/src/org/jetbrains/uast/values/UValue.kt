package org.jetbrains.uast.values

interface UValue : UOperand {

    fun merge(other: UValue): UValue

    val dependencies: Set<UDependency>
        get() = emptySet()

    fun toConstant(): UConstant?

    val reachable: Boolean

    companion object {
        val UNREACHABLE: UValue = UNothingValue()
    }
}

fun UValue.toPossibleConstants(): Set<UConstant> {
    val results = mutableSetOf<UConstant>()
    toPossibleConstants(results)
    return results
}

private fun UValue.toPossibleConstants(result: MutableSet<UConstant>) {
    when (this) {
        is UDependentValue -> value.toPossibleConstants(result)
        is UPhiValue -> values.forEach { it.toPossibleConstants(result) }
        else -> toConstant()?.let { result.add(it) }
    }
}
