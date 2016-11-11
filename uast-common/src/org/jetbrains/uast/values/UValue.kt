package org.jetbrains.uast.values

import com.intellij.psi.PsiType
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UResolvable
import org.jetbrains.uast.UVariable

sealed class UValue : UOperand {

    // Constants

    abstract class AbstractConstant : UValue(), UConstant {
        override fun valueEquals(other: UValue) = when (other) {
            this -> UBooleanConstant.True
            is UValue.AbstractConstant -> UBooleanConstant.False
            else -> super.valueEquals(other)
        }

        override fun equals(other: Any?) = other is AbstractConstant && value == other.value

        override fun hashCode() = value?.hashCode() ?: 0

        override fun toString() = "$value"

        override fun asString() = toString()
    }

    // Dependencies and dependents

    interface Dependency

    open class Dependent protected constructor(
            val value: UValue,
            override val dependencies: Set<Dependency> = emptySet()
    ) : UValue() {

        private fun UValue.unwrap() = (this as? Dependent)?.unwrap() ?: this

        private fun unwrap(): UValue = value.unwrap()

        private val dependenciesWithThis: Set<Dependency>
            get() = (this as? Dependency)?.let { dependencies + it } ?: dependencies

        private fun wrapBinary(result: UValue, arg: UValue): UValue {
            val wrappedDependencies = (arg as? Dependent)?.dependenciesWithThis ?: emptySet()
            val resultDependencies = dependenciesWithThis + wrappedDependencies
            return create(result, resultDependencies)
        }

        private fun wrapUnary(result: UValue) = create(result, dependenciesWithThis)

        override fun plus(other: UValue) = wrapBinary(unwrap() + other.unwrap(), other)

        override fun minus(other: UValue) = wrapBinary(unwrap() - other.unwrap(), other)

        override fun times(other: UValue) = wrapBinary(unwrap() * other.unwrap(), other)

        override fun div(other: UValue) = wrapBinary(unwrap() / other.unwrap(), other)

        internal fun inverseDiv(other: UValue) = wrapBinary(other.unwrap() / unwrap(), other)

        override fun mod(other: UValue) = wrapBinary(unwrap() % other.unwrap(), other)

        internal fun inverseMod(other: UValue) = wrapBinary(other.unwrap() % unwrap(), other)

        override fun unaryMinus() = wrapUnary(-unwrap())

        override fun valueEquals(other: UValue) = wrapBinary(unwrap() valueEquals other.unwrap(), other)

        override fun valueNotEquals(other: UValue) = wrapBinary(unwrap() valueNotEquals other.unwrap(), other)

        override fun not() = wrapUnary(!unwrap())

        override fun greater(other: UValue) = wrapBinary(unwrap() greater other.unwrap(), other)

        override fun less(other: UValue) = wrapBinary(other.unwrap() greater unwrap(), other)

        override fun inc() = wrapUnary(unwrap().inc())

        override fun dec() = wrapUnary(unwrap().dec())

        override fun and(other: UValue) = wrapBinary(unwrap() and other.unwrap(), other)

        override fun or(other: UValue) = wrapBinary(unwrap() or other.unwrap(), other)

        override fun bitwiseAnd(other: UValue) = wrapBinary(unwrap() bitwiseAnd other.unwrap(), other)

        override fun bitwiseOr(other: UValue) = wrapBinary(unwrap() bitwiseOr other.unwrap(), other)

        override fun bitwiseXor(other: UValue) = wrapBinary(unwrap() bitwiseXor other.unwrap(), other)

        override fun shl(other: UValue) = wrapBinary(unwrap() shl other.unwrap(), other)

        internal fun inverseShiftLeft(other: UValue) = wrapBinary(other.unwrap() shl unwrap(), other)

        override fun shr(other: UValue) = wrapBinary(unwrap() shr other.unwrap(), other)

        internal fun inverseShiftRight(other: UValue) = wrapBinary(other.unwrap() shr unwrap(), other)

        override fun ushr(other: UValue) = wrapBinary(unwrap() ushr other.unwrap(), other)

        internal fun inverseShiftRightUnsigned(other: UValue) =
                wrapBinary(other.unwrap() ushr unwrap(), other)

        override fun merge(other: UValue) = when (other) {
            this -> this
            value -> this
            is Dependent -> {
                if (value != other.value) Phi.create(this, other)
                else Dependent(value, dependencies + other.dependencies)
            }
            else -> Phi.create(this, other)
        }

        override fun toConstant() = value.toConstant()

        override fun toVariable() = value.toVariable()

        open internal fun copy(dependencies: Set<Dependency>) =
                if (dependencies == this.dependencies) this else create(value, dependencies)

        override fun replaceConstant(constant: AbstractConstant): UValue =
                if (toConstant() == constant) this
                else create(value.replaceConstant(constant), dependencies)

        override fun equals(other: Any?) =
                other is Dependent
                && javaClass == other.javaClass
                && value == other.value
                && dependencies == other.dependencies

        override fun hashCode(): Int {
            var result = 31
            result = result * 19 + value.hashCode()
            result = result * 19 + dependencies.hashCode()
            return result
        }

        override fun toString() =
                if (dependencies.isNotEmpty())
                    "$value" + dependencies.joinToString(prefix = " (depending on: ", postfix = ")", separator = ", ")
                else
                    "$value"

        companion object {
            fun create(value: UValue, dependencies: Set<Dependency>): UValue =
                    if (dependencies.isNotEmpty()) Dependent(value, dependencies)
                    else value
        }
    }

    // Value of some (possibly evaluable) variable
    class Variable private constructor(
            val variable: UVariable,
            value: UValue,
            dependencies: Set<Dependency>
    ) : Dependent(value, dependencies), Dependency {

        override fun identityEquals(other: UValue): UValue =
                when (variable.psi.type) {
                    PsiType.BYTE, PsiType.FLOAT, PsiType.DOUBLE, PsiType.LONG,
                    PsiType.SHORT, PsiType.INT, PsiType.CHAR, PsiType.BOOLEAN -> super.identityEquals(other)

                    else -> Undetermined
                }

        override fun merge(other: UValue) = when (other) {
            this -> this
            value -> this
            is Variable -> {
                if (variable != other.variable || value != other.value) Phi.create(this, other)
                else create(variable, value, dependencies + other.dependencies)
            }
            is Dependent -> {
                if (value != other.value) Phi.create(this, other)
                else create(variable, value, dependencies + other.dependencies)
            }
            else -> Phi.create(this, other)
        }

        override fun copy(dependencies: Set<Dependency>) =
                if (dependencies == this.dependencies) this else create(variable, value, dependencies)

        override fun replaceConstant(constant: AbstractConstant): UValue =
                if (constant == toConstant()) this
                else create(variable, value.replaceConstant(constant), dependencies)

        override fun equals(other: Any?) =
                other is Variable
                && variable == other.variable
                && value == other.value
                && dependencies == other.dependencies

        override fun hashCode(): Int {
            var result = 31
            result = result * 19 + variable.hashCode()
            result = result * 19 + value.hashCode()
            result = result * 19 + dependencies.hashCode()
            return result
        }

        override fun toString() = "(var ${variable.name ?: "<unnamed>"} = ${super.toString()})"

        companion object {

            private fun Set<Dependency>.filterNotVariable(variable: UVariable) =
                    filterTo(linkedSetOf()) { it !is Variable || variable != it.variable }

            fun create(variable: UVariable, value: UValue, dependencies: Set<Dependency> = emptySet()): Variable {
                when (variable.psi.type) {
                    PsiType.BYTE, PsiType.SHORT -> {
                        val constant = value.toConstant()
                        if (constant is UIntConstant && constant.type == UNumericType.INT) {
                            val castConstant = UIntConstant(constant.value, variable.psi.type)
                            return create(variable, value.replaceConstant(castConstant), dependencies)
                        }
                    }
                }
                val dependenciesWithoutSelf = dependencies.filterNotVariable(variable)
                return when {
                    value is Variable
                    && variable == value.variable
                    && dependenciesWithoutSelf == value.dependencies -> value

                    value is Dependent -> {
                        val valueDependencies = value.dependencies.filterNotVariable(variable)
                        val modifiedValue = value.copy(valueDependencies)
                        Variable(variable, modifiedValue, dependenciesWithoutSelf)
                    }

                    else -> Variable(variable, value, dependenciesWithoutSelf)
                }
            }
        }
    }

    // Value of something resolvable (e.g. call or property access)
    // that we cannot or do not want to evaluate
    class CallResult(val resolvable: UResolvable) : UValue(), Dependency {
        override fun equals(other: Any?) = other is CallResult && resolvable == other.resolvable

        override fun hashCode() = resolvable.hashCode()

        override fun toString(): String {
            return "external ${(resolvable as? UElement)?.asRenderString() ?: "???"}"
        }
    }

    class Phi private constructor(val values: Set<UValue>): UValue() {

        override val dependencies: Set<Dependency> = values.flatMapTo(linkedSetOf()) { it.dependencies }

        override fun equals(other: Any?) = other is Phi && values == other.values

        override fun hashCode() = values.hashCode()

        override fun toString() = values.joinToString(prefix = "Phi(", postfix = ")", separator = ", ")

        companion object {
            fun create(values: Iterable<UValue>): Phi {
                val flattenedValues = values.flatMapTo(linkedSetOf<UValue>()) { (it as? Phi)?.values ?: listOf(it) }
                if (flattenedValues.size <= 1) {
                    throw AssertionError("Phi should contain two or more values: $flattenedValues")
                }
                return Phi(flattenedValues)
            }

            fun create(vararg values: UValue) = create(values.asIterable())
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

    override operator fun times(other: UValue): UValue = if (other is Dependent) other * this else Undetermined

    override operator fun div(other: UValue): UValue = (other as? Dependent)?.inverseDiv(this) ?: Undetermined

    override operator fun mod(other: UValue): UValue = (other as? Dependent)?.inverseMod(this) ?: Undetermined

    override fun unaryMinus(): UValue = Undetermined

    override fun valueEquals(other: UValue): UValue =
            if (other is Dependent || other is UNaNConstant) other.valueEquals(this) else Undetermined

    override fun valueNotEquals(other: UValue): UValue = !this.valueEquals(other)

    override fun identityEquals(other: UValue): UValue = valueEquals(other)

    override fun identityNotEquals(other: UValue): UValue = !this.identityEquals(other)

    override fun not(): UValue = Undetermined

    override fun greater(other: UValue): UValue =
            if (other is Dependent || other is UNaNConstant) other.less(this) else Undetermined

    override fun less(other: UValue): UValue = other.greater(this)

    override fun greaterOrEquals(other: UValue) = this.greater(other) or this.valueEquals(other)

    override fun lessOrEquals(other: UValue) = this.less(other) or this.valueEquals(other)

    override fun inc(): UValue = Undetermined

    override fun dec(): UValue = Undetermined

    override fun and(other: UValue): UValue = if (other is Dependent) other and this else Undetermined

    override fun or(other: UValue): UValue = if (other is Dependent) other or this else Undetermined

    override fun bitwiseAnd(other: UValue): UValue = if (other is Dependent) other bitwiseAnd this else Undetermined

    override fun bitwiseOr(other: UValue): UValue = if (other is Dependent) other bitwiseOr this else Undetermined

    override fun bitwiseXor(other: UValue): UValue = if (other is Dependent) other bitwiseXor this else Undetermined

    override fun shl(other: UValue): UValue = (other as? Dependent)?.inverseShiftLeft(this) ?: Undetermined

    override fun shr(other: UValue): UValue = (other as? Dependent)?.inverseShiftRight(this) ?: Undetermined

    override fun ushr(other: UValue): UValue =
            (other as? Dependent)?.inverseShiftRightUnsigned(this) ?: Undetermined

    open fun merge(other: UValue): UValue = when (other) {
        this -> this
        is Variable -> other.merge(this)
        else -> Phi.create(this, other)
    }

    open val dependencies: Set<Dependency>
        get() = emptySet()

    open fun toConstant(): UConstant? = this as? AbstractConstant

    internal open fun replaceConstant(constant: AbstractConstant): UValue = constant

    open fun toVariable(): Variable? = this as? Variable

    override abstract fun toString(): String
}