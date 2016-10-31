package org.jetbrains.uast.values

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UResolvable
import org.jetbrains.uast.UVariable

sealed class UValue : UOperand {

    // Constants

    abstract class AbstractConstant(override val value: Any?) : UValue(), UConstant {
        override fun same(other: UValue) = when (other) {
            this -> UBooleanConstant.True
            is UValue.AbstractConstant -> UBooleanConstant.False
            else -> super.same(other)
        }

        override fun equals(other: Any?) = other is AbstractConstant && value == other.value

        override fun hashCode() = value?.hashCode() ?: 0

        override fun toString() = "$value"
    }

    // Dependencies and dependents

    interface Dependency

    open class Dependent protected constructor(
            val value: UValue,
            override val dependencies: List<Dependency> = emptyList()
    ) : UValue() {

        private fun UValue.unwrap() = (this as? Dependent)?.unwrap() ?: this

        private fun unwrap(): UValue = value.unwrap()

        private val dependenciesWithThis: List<Dependency>
            get() = (this as? Dependency)?.let { dependencies + it } ?: dependencies

        private fun wrapBinary(result: UValue, arg: UValue): UValue {
            val wrappedDependencies = (arg as? Dependent)?.dependenciesWithThis ?: emptyList()
            val resultDependencies = dependenciesWithThis + wrappedDependencies
            return create(result, resultDependencies)
        }

        private fun wrapUnary(result: UValue) = create(result, dependenciesWithThis)

        override fun plus(other: UValue) = wrapBinary(unwrap() + other.unwrap(), other)

        override fun minus(other: UValue) = wrapBinary(unwrap() - other.unwrap(), other)

        override fun unaryMinus() = wrapUnary(-value)

        override fun same(other: UValue) = wrapBinary(unwrap() same other.unwrap(), other)

        override fun notSame(other: UValue) = wrapBinary(unwrap() notSame other.unwrap(), other)

        override fun not() = wrapUnary(!value)

        override fun merge(other: UValue) = when (other) {
            this -> this
            value -> this
            else -> Phi.create(this, other)
        }

        override fun toConstant() = value.toConstant()

        override fun toVariable() = value.toVariable()

        override fun toString() =
                if (dependencies.isNotEmpty())
                    "$value" + dependencies.joinToString(prefix = " (depending on: ", postfix = ")", separator = ", ")
                else
                    "$value"

        companion object {
            fun create(value: UValue, dependencies: List<Dependency>): UValue =
                    if (dependencies.isNotEmpty()) Dependent(value, dependencies)
                    else value
        }
    }

    // Value of some (possibly evaluable) variable
    class Variable(
            val variable: UVariable,
            value: UValue,
            dependencies: List<Dependency> = emptyList()
    ) : Dependent(value, dependencies), Dependency {

        override fun toString() = "(var ${variable.name ?: "<unnamed>"} = ${super.toString()})"
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

    class Phi private constructor(val values: List<UValue>): UValue() {

        override val dependencies: List<Dependency> = values.flatMap { it.dependencies }

        override fun equals(other: Any?) = other is Phi && values == other.values

        override fun hashCode() = values.hashCode()

        override fun toString() = values.joinToString(prefix = "Phi(", postfix = ")", separator = ", ")

        companion object {
            fun create(values: List<UValue>): Phi {
                return Phi(values.map { (it as? Phi)?.values ?: listOf(it) }.flatten())
            }

            fun create(vararg values: UValue) = create(values.toList())
        }
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

    override fun same(other: UValue): UValue = if (other is Dependent) other same this else Undetermined

    override fun notSame(other: UValue): UValue = !this.same(other)

    override fun not(): UValue = Undetermined

    open fun merge(other: UValue): UValue = when (other) {
        this -> this
        is Variable -> other.merge(this)
        else -> Phi.create(this, other)
    }

    open val dependencies: List<Dependency>
        get() = emptyList()

    open fun toConstant(): UConstant? = this as? UConstant

    open fun toVariable(): Variable? = this as? Variable

    override fun toString(): String = throw AssertionError("toString() is not overridden in ${this.javaClass} UValue")
}