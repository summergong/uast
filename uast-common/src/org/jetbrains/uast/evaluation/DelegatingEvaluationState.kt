package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.values.UValue

class DelegatingEvaluationState(
        boundElement: UElement,
        private val variableValue: UValue.Variable,
        private val baseState: UEvaluationState
) : AbstractEvaluationState(boundElement) {

    override fun get(variable: UVariable) =
            if (variable.psi == variableValue.variable.psi) variableValue.value else baseState[variable]
}