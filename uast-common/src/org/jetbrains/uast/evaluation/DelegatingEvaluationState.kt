package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.values.UVariableValue

class DelegatingEvaluationState(
        boundElement: UElement,
        private val variableValue: UVariableValue,
        private val baseState: UEvaluationState
) : AbstractEvaluationState(boundElement) {

    override val variables = baseState.variables + variableValue.variable

    override fun get(variable: UVariable) =
            if (variable == variableValue.variable) variableValue else baseState[variable]
}