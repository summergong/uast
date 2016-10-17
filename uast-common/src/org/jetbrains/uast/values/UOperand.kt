package org.jetbrains.uast.values

interface UOperand {
    operator fun plus(other: UValue): UValue

    operator fun minus(other: UValue): UValue

    operator fun unaryMinus(): UValue
}