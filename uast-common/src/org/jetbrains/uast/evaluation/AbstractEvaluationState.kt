package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.values.UValue

abstract class AbstractEvaluationState(override val boundElement: UElement? = null) : UEvaluationState {
    override fun assign(variable: UVariable, value: UValue, at: UElement) =
            DelegatingEvaluationState(
                    boundElement = at,
                    variableValue = UValue.Variable(variable, value),
                    baseState = this
            )

    override fun merge(otherState: UEvaluationState) = MergingEvaluationState(this, otherState)
}