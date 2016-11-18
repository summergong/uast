package org.jetbrains.uast.values

interface UValue : UOperand {

    fun merge(other: UValue): UValue

    val dependencies: Set<UDependency>
        get() = emptySet()

    fun toConstant(): UConstant?

    val reachable: Boolean
}