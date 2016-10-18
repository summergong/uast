package org.jetbrains.uast.evaluation

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.values.UValue

// Role: stores current values for all variables (and may be something else)
// Immutable
interface UEvaluationState {
    val boundElement: UElement?

    operator fun get(variable: UVariable): UValue

    // Creates new evaluation state with state[variable] = value and boundElement = at
    fun assign(variable: UVariable, value: UValue, at: UElement): UEvaluationState

    // Merged two states
    fun merge(otherState: UEvaluationState): UEvaluationState
}

fun UElement.createEmptyState(): UEvaluationState = EmptyEvaluationState(this)