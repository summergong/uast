package org.jetbrains.uast.values

interface UOperand {
    operator fun plus(other: UValue): UValue

    operator fun minus(other: UValue): UValue

    operator fun unaryMinus(): UValue

    operator fun not(): UValue

    infix fun same(other: UValue): UValue

    infix fun notSame(other: UValue): UValue

    infix fun identitySame(other: UValue): UValue = same(other)

    infix fun identityNotSame(other: UValue): UValue = notSame(other)
}