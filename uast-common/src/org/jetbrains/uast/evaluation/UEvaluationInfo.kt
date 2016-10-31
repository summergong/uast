package org.jetbrains.uast.evaluation

import org.jetbrains.uast.values.UValue

data class UEvaluationInfo(val value: UValue, val state: UEvaluationState) {
    fun merge(otherInfo: UEvaluationInfo): UEvaluationInfo {
        val mergedValue = value.merge(otherInfo.value)
        val mergedState = state.merge(otherInfo.state)
        return UEvaluationInfo(mergedValue, mergedState)
    }

    fun changeValue(newValue: UValue) = UEvaluationInfo(newValue, state)
}