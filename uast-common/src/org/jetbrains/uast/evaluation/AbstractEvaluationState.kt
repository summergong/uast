package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.values.UValue
import org.jetbrains.uast.values.UVariableValue

abstract class AbstractEvaluationState(override val boundElement: UElement? = null) : UEvaluationState {
    override fun assign(variable: UVariable, value: UValue, at: UElement): AbstractEvaluationState {
        val variableValue = UVariableValue.create(variable, value)
        val prevVariableValue = this[variable]
        return if (prevVariableValue == variableValue) this
        else DelegatingEvaluationState(
                boundElement = at,
                variableValue = variableValue,
                baseState = this
        )
    }

    override fun merge(otherState: UEvaluationState) =
            if (this == otherState) this else MergingEvaluationState(this, otherState)

    override fun equals(other: Any?) =
            other is UEvaluationState && variables == other.variables && variables.all { this[it] == other[it] }

    override fun hashCode(): Int {
        var result = 31
        result = result * 19 + variables.hashCode()
        result = result * 19 + variables.map { this[it].hashCode() }.sum()
        return result
    }

    override fun toString() = variables.joinToString(prefix = "[", postfix = "]", separator = ", ") {
        "${it.psi.name} = ${this[it]}"
    }
}