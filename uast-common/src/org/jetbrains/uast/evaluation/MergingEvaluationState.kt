package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable

class MergingEvaluationState(
        private val first: UEvaluationState,
        private val second: UEvaluationState
) : AbstractEvaluationState() {

    override fun get(variable: UVariable) = first[variable].merge(second[variable])
}