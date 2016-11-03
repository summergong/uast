package org.jetbrains.uast.values

import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiType
import org.jetbrains.uast.name

interface UConstant {
    val value: Any?
}

// IntValue?
class UIntConstant(override val value: Long, val bytes: Int = 8) : UValue.AbstractConstant(value) {
    override fun plus(other: UValue) = when (other) {
        is UIntConstant -> UIntConstant(value + other.value, Math.max(bytes, other.bytes))
        is UFloatConstant -> other + this
        else -> super.plus(other)
    }

    override fun unaryMinus() = UIntConstant(-value, bytes)

    override fun greater(other: UValue) = when (other) {
        is UIntConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        is UFloatConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        else -> super.greater(other)
    }

    override fun toString() = "$value ($bytes " + if (bytes == 1) "byte)" else "bytes)"

    override fun asString() = "$value"
}

class UFloatConstant(override val value: Double) : UValue.AbstractConstant(value) {
    override fun plus(other: UValue) = when (other) {
        is UIntConstant -> UFloatConstant(value + other.value)
        is UFloatConstant -> UFloatConstant(value + other.value)
        else -> super.plus(other)
    }

    override fun greater(other: UValue) = when (other) {
        is UIntConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        is UFloatConstant -> if (value > other.value) UBooleanConstant.True else UBooleanConstant.False
        else -> super.greater(other)
    }

    override fun unaryMinus() = UFloatConstant(-value)
}

sealed class UBooleanConstant(override val value: Boolean) : UValue.AbstractConstant(value) {
    object True : UBooleanConstant(true) {
        override fun not() = False
    }

    object False : UBooleanConstant(false) {
        override fun not() = True
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
