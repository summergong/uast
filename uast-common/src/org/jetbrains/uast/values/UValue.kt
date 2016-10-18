package org.jetbrains.uast.values

import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiType
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UResolvable
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.name

sealed class UValue : UOperand {

    // Constants

    interface Constant {
        val value: Any?
    }

    abstract class AbstractConstant(override val value: Any?) : UValue(), Constant {
        override final fun equals(other: Any?) = other is AbstractConstant && value == other.value

        override final fun hashCode() = value?.hashCode() ?: 0

        override fun toString() = "$value"
    }

    // IntValue?
    class NumericInt(override val value: Long, val bytes: Int = 8) : AbstractConstant(value) {
        override fun plus(other: UValue) = when (other) {
            is NumericInt -> NumericInt(value + other.value, Math.max(bytes, other.bytes))
            is NumericFloat -> other + this
            else -> super.plus(other)
        }

        override fun unaryMinus() = NumericInt(-value, bytes)

        override fun toString() = "$value ($bytes " + if (bytes == 1) "byte)" else "bytes)"
    }

    class NumericFloat(override val value: Double) : AbstractConstant(value) {
        override fun plus(other: UValue) = when (other) {
            is NumericInt -> NumericFloat(value + other.value)
            is NumericFloat -> NumericFloat(value + other.value)
            else -> super.plus(other)
        }

        override fun unaryMinus() = NumericFloat(-value)
    }

    class Bool(override val value: Boolean) : AbstractConstant(value)

    class EnumEntry(override val value: PsiEnumConstant) : AbstractConstant(value) {
        override fun toString() = value.name ?: "<unnamed enum entry>"
    }

    class ClassLiteral(override val value: PsiType) : AbstractConstant(value) {
        override fun toString() = value.name
    }

    object Null : AbstractConstant(null)

    // Dependencies and dependents

    interface Dependency

    open class Dependent(
            val value: UValue,
            override val dependencies: List<Dependency> = emptyList()
    ) : UValue() {

        private fun UValue.unwrap() = (this as? Dependent)?.unwrap() ?: this

        private fun unwrap(): UValue = value.unwrap()

        private fun wrapBinary(result: UValue, arg: UValue): Dependent {
            val wrappedDependencies = (arg as? Dependent)?.dependencies ?: emptyList()
            val resultDependencies =
                    if (wrappedDependencies.isNotEmpty()) dependencies + wrappedDependencies else dependencies
            return Dependent(result, resultDependencies)
        }

        override fun plus(other: UValue) = wrapBinary(unwrap() + other.unwrap(), other)

        override fun minus(other: UValue) = wrapBinary(unwrap() - other.unwrap(), other)

        override fun unaryMinus() = Dependent(-value, dependencies)

        override fun merge(other: UValue) = when (other) {
            this -> this
            is Variable -> other.merge(this)
            value -> this
            else -> Phi(this, other)
        }

        override fun toConstant() = value.toConstant()

        override fun toVariable() = value.toVariable()

        override fun toString() =
                if (dependencies.isNotEmpty())
                    "$value" + dependencies.joinToString(prefix = " (depending on: ", postfix = ")", separator = ", ")
                else
                    "$value"
    }

    // Value of some (possibly evaluable) variable
    class Variable(
            val variable: UVariable,
            value: UValue,
            dependencies: List<Dependency> = emptyList()
    ) : Dependent(value, dependencies), Dependency {

        override fun merge(other: UValue): UValue = when (other) {
            is Dependent -> merge(other.value)
            else -> super.merge(other)
        }

        override fun toString() = "var ${variable.name ?: "<unnamed>"} = " + super.toString()
    }

    // Value of something resolvable (e.g. call or property access)
    // that we cannot or do not want to evaluate
    class External(val resolvable: UResolvable) : UValue(), Dependency {
        override fun equals(other: Any?) = other is External && resolvable == other.resolvable

        override fun hashCode() = resolvable.hashCode()

        override fun toString(): String {
            return "external ${(resolvable as? UElement)?.asRenderString() ?: "???"}"
        }
    }

    class Phi(val values: List<UValue>): UValue() {

        constructor(vararg values: UValue) : this(values.toList())

        override val dependencies: List<Dependency> = values.flatMap { it.dependencies }

        override fun equals(other: Any?) = other is Phi && values == other.values

        override fun hashCode() = values.hashCode()

        override fun toString() = values.joinToString(prefix = "Phi(", postfix = ")", separator = ", ")
    }

    // Miscellaneous

    // Something that never can be created
    object Nothing : UValue() {
        override fun toString() = "Nothing"
    }

    // Something with value that cannot be evaluated
    object Undetermined : UValue() {
        override fun toString() = "Undetermined"
    }

    // Methods

    override operator fun plus(other: UValue): UValue = if (other is Dependent) other + this else Undetermined

    override operator fun minus(other: UValue): UValue = this + (-other)

    override fun unaryMinus(): UValue = Undetermined

    open fun merge(other: UValue): UValue = when (other) {
        this -> this
        is Variable -> other.merge(this)
        else -> Phi(this, other)
    }

    open val dependencies: List<Dependency>
        get() = emptyList()

    open fun toConstant(): Constant? = this as? Constant

    open fun toVariable(): Variable? = this as? Variable

    override fun toString(): String = throw AssertionError("toString() is not overridden in ${this.javaClass} UValue")
}