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

    override fun toString() = "$value ($bytes " + if (bytes == 1) "byte)" else "bytes)"
}

class UFloatConstant(override val value: Double) : UValue.AbstractConstant(value) {
    override fun plus(other: UValue) = when (other) {
        is UIntConstant -> UFloatConstant(value + other.value)
        is UFloatConstant -> UFloatConstant(value + other.value)
        else -> super.plus(other)
    }

    override fun unaryMinus() = UFloatConstant(-value)
}

sealed class UBooleanConstant(override val value: Boolean) : UValue.AbstractConstant(value) {
    object True : UBooleanConstant(true)

    object False : UBooleanConstant(false)

    companion object {
        fun valueOf(value: Boolean) = if (value) True else False
    }
}

class UEnumEntryValueConstant(override val value: PsiEnumConstant) : UValue.AbstractConstant(value) {
    override fun toString() = value.name?.let { "$it (enum entry)" }?: "<unnamed enum entry>"
}

class UClassConstant(override val value: PsiType) : UValue.AbstractConstant(value) {
    override fun toString() = value.name
}

object UNullConstant : UValue.AbstractConstant(null)
