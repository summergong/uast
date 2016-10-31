package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.values.UValue

class EmptyEvaluationState(boundElement: UElement) : AbstractEvaluationState(boundElement) {
    override val variables: Set<UVariable>
        get() = emptySet()

    override fun get(variable: UVariable) = UValue.Undetermined
}