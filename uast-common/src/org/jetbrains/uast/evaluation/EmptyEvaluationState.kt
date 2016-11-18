package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.values.UUndeterminedValue

class EmptyEvaluationState(boundElement: UElement) : AbstractEvaluationState(boundElement) {
    override val variables: Set<UVariable>
        get() = emptySet()

    override fun get(variable: UVariable) = UUndeterminedValue
}