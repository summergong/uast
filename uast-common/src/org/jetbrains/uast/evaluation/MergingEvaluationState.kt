package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UVariable

class MergingEvaluationState(
        private val first: UEvaluationState,
        private val second: UEvaluationState
) : AbstractEvaluationState() {

    override val variables: Set<UVariable>
        get() = first.variables + second.variables

    override fun get(variable: UVariable) = first[variable].merge(second[variable])
}