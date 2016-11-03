package org.jetbrains.uast.values

import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiType
import org.jetbrains.uast.name

interface UConstant {
    val value: Any?
}

abstract class UNumericConstant(override val value: Number) : UValue.AbstractConstant(value)

class UIntConstant(override val value: Int) : UNumericConstant(value) {
    override fun plus(other: UValue) = when (other) {
        is UIntConstant -> UIntConstant(value + other.value)
        is ULongConstant -> other + this
        is UFloatConstant -> other + this
        else -> super.plus(other)
    }

    override fun times(other: UValue) = when (other) {
        is UIntConstant -> UIntConstant(value * other.value)
        is ULongConstant -> other * this
        is UFloatConstant -> other * this
        else -> super.times(other)
    }

    override fun div(other: UValue) = when (other) {
        is UIntConstant -> UIntConstant(value / other.value)
        is ULongConstant -> ULongConstant(value / other.value)
        is UFloatConstant -> UFloatConstant(value / other.value)
        else -> super.div(other)
    }

    override fun mod(other: UValue) = when (other) {
        is UIntConstant -> UIntConstant(value % other.value)
        is ULongConstant -> ULongConstant(value % other.value)
        is UFloatConstant -> UFloatConstant(value % other.value)
        else -> super.mod(other)
    }

    override fun unaryMinus() = UIntConstant(-value)

    override fun greater(other: UValue) = when (other) {
        is UIntConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        is ULongConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        is UFloatConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        else -> super.greater(other)
    }

    override fun inc() = UIntConstant(value + 1)

    override fun dec() = UIntConstant(value - 1)

    override fun toString() = "$value"

    override fun asString() = "$value"
}

class ULongConstant(override val value: Long) : UNumericConstant(value) {
    override fun plus(other: UValue) = when (other) {
        is ULongConstant -> ULongConstant(value + other.value)
        is UIntConstant -> ULongConstant(value + other.value)
        is UFloatConstant -> other + this
        else -> super.plus(other)
    }

    override fun times(other: UValue) = when (other) {
        is ULongConstant -> ULongConstant(value * other.value)
        is UIntConstant -> ULongConstant(value * other.value)
        is UFloatConstant -> other * this
        else -> super.times(other)
    }

    override fun div(other: UValue) = when (other) {
        is ULongConstant -> ULongConstant(value / other.value)
        is UIntConstant -> ULongConstant(value / other.value)
        is UFloatConstant -> UFloatConstant(value / other.value)
        else -> super.div(other)
    }

    override fun mod(other: UValue) = when (other) {
        is ULongConstant -> ULongConstant(value % other.value)
        is UIntConstant -> ULongConstant(value % other.value)
        is UFloatConstant -> UFloatConstant(value % other.value)
        else -> super.mod(other)
    }

    override fun unaryMinus() = ULongConstant(-value)

    override fun greater(other: UValue) = when (other) {
        is ULongConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        is UIntConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        is UFloatConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        else -> super.greater(other)
    }

    override fun inc() = ULongConstant(value + 1)

    override fun dec() = ULongConstant(value - 1)

    override fun toString() = "${value}L"

    override fun asString() = "$value"
}

class UFloatConstant(override val value: Double) : UNumericConstant(value) {
    override fun plus(other: UValue) = when (other) {
        is ULongConstant -> UFloatConstant(value + other.value)
        is UIntConstant -> UFloatConstant(value + other.value)
        is UFloatConstant -> UFloatConstant(value + other.value)
        else -> super.plus(other)
    }

    override fun times(other: UValue) = when (other) {
        is ULongConstant -> UFloatConstant(value * other.value)
        is UIntConstant -> UFloatConstant(value * other.value)
        is UFloatConstant -> UFloatConstant(value * other.value)
        else -> super.times(other)
    }

    override fun div(other: UValue) = when (other) {
        is ULongConstant -> UFloatConstant(value / other.value)
        is UIntConstant -> UFloatConstant(value / other.value)
        is UFloatConstant -> UFloatConstant(value / other.value)
        else -> super.div(other)
    }

    override fun mod(other: UValue) = when (other) {
        is ULongConstant -> UFloatConstant(value % other.value)
        is UIntConstant -> UFloatConstant(value % other.value)
        is UFloatConstant -> UFloatConstant(value % other.value)
        else -> super.mod(other)
    }

    override fun greater(other: UValue) = when (other) {
        is ULongConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        is UIntConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        is UFloatConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        else -> super.greater(other)
    }

    override fun unaryMinus() = UFloatConstant(-value)

    override fun inc() = UFloatConstant(value + 1)

    override fun dec() = UFloatConstant(value - 1)
}

sealed class UBooleanConstant(override val value: Boolean) : UValue.AbstractConstant(value) {
    object True : UBooleanConstant(true) {
        override fun not() = False

        override fun and(other: UValue) = other as? UBooleanConstant ?: super.and(other)

        override fun or(other: UValue) = True

        override fun xor(other: UValue) = (other as? UBooleanConstant)?.not() ?: super.xor(other)
    }

    object False : UBooleanConstant(false) {
        override fun not() = True

        override fun and(other: UValue) = False

        override fun or(other: UValue) = other as? UBooleanConstant ?: super.or(other)

        override fun xor(other: UValue) = other as? UBooleanConstant ?: super.xor(other)
    }

    companion object {
        fun valueOf(value: Boolean) = if (value) True else False
    }
}

class UStringConstant(override val value: String) : UValue.AbstractConstant(value) {

    override fun plus(other: UValue) = when (other) {
        is UValue.AbstractConstant -> UStringConstant(value + other.asString())
        else -> super.plus(other)
    }

    override fun greater(other: UValue) = when (other) {
        is UStringConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        else -> super.greater(other)
    }

    override fun asString() = value

    override fun toString() = "\"$value\""
}

class UEnumEntryValueConstant(override val value: PsiEnumConstant) : UValue.AbstractConstant(value) {
    override fun equals(other: Any?) =
            other is UEnumEntryValueConstant &&
            value.nameIdentifier.text == other.value.nameIdentifier.text &&
            value.containingClass?.qualifiedName == other.value.containingClass?.qualifiedName

    override fun hashCode(): Int {
        var result = 19
        result = result * 13 + value.nameIdentifier.text.hashCode()
        result = result * 13 + (value.containingClass?.qualifiedName?.hashCode() ?: 0)
        return result
    }

    override fun toString() = value.name?.let { "$it (enum entry)" }?: "<unnamed enum entry>"

    override fun asString() = value.name ?: ""
}

class UClassConstant(override val value: PsiType) : UValue.AbstractConstant(value) {
    override fun toString() = value.name
}

object UNullConstant : UValue.AbstractConstant(null)
