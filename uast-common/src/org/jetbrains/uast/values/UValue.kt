package org.jetbrains.uast.values

import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiType
import org.jetbrains.uast.UResolvable

private val FLOAT_ZERO = UValue.NumericFloat(0.0)

sealed class UValue {
    class NumericInt(val value: Long, val bytes: Int = 8) : UValue() {
        override fun plus(other: UValue) = when (other) {
            is NumericInt -> NumericInt(value + other.value, Math.max(bytes, other.bytes))
            is NumericFloat -> other + this
            else -> Undetermined
        }

        override fun minus(other: UValue) = when (other) {
            is NumericInt -> NumericInt(value - other.value, Math.max(bytes, other.bytes))
            is NumericFloat -> FLOAT_ZERO - (other - this)
            else -> Undetermined
        }

        override fun equals(other: Any?) = other is NumericInt && value == other.value

        override fun hashCode() = value.hashCode()
    }

    class NumericFloat(val value: Double) : UValue() {
        override fun plus(other: UValue) = when (other) {
            is NumericInt -> NumericFloat(value + other.value)
            is NumericFloat -> NumericFloat(value + other.value)
            else -> Undetermined
        }

        override fun minus(other: UValue) = when (other) {
            is NumericInt -> NumericFloat(value + other.value)
            is NumericFloat -> NumericFloat(value + other.value)
            else -> Undetermined
        }

        override fun equals(other: Any?) = other is NumericFloat && value == other.value

        override fun hashCode() = value.hashCode()
    }

    class Bool(val value: Boolean) : UValue() {
        override fun equals(other: Any?) = other is Bool && value == other.value

        override fun hashCode() = value.hashCode()
    }

    class EnumEntry(val enumConstant: PsiEnumConstant) : UValue() {
        override fun equals(other: Any?) = other is EnumEntry && enumConstant == other.enumConstant

        override fun hashCode() = enumConstant.hashCode()
    }

    class ClassLiteral(val type: PsiType) : UValue() {
        override fun equals(other: Any?) = other is ClassLiteral && type == other.type

        override fun hashCode() = type.hashCode()
    }

    object Null : UValue()

    object Nothing : UValue()

    // Something with value that cannot be evaluated
    object Undetermined : UValue()

    // Value of something resolvable (e.g. call or property access)
    // that we cannot or do not want to evaluate
    class External(val resolvable: UResolvable) : UValue() {
        override fun equals(other: Any?) = other is External && resolvable == other.resolvable

        override fun hashCode() = resolvable.hashCode()
    }

    open operator fun plus(other: UValue): UValue = Undetermined

    open operator fun minus(other: UValue): UValue = Undetermined

    open fun merge(other: UValue): UValue = if (this == other) this else Undetermined
}