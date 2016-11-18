package org.jetbrains.uast.evaluation

import org.jetbrains.uast.values.UValue

data class UEvaluationInfo(val value: UValue, val state: UEvaluationState) {
    fun merge(otherInfo: UEvaluationInfo): UEvaluationInfo {
        // info with 'UNothingValue' is just ignored, if other is not UNothingValue
        if (!reachable && otherInfo.reachable) return otherInfo
        if (!otherInfo.reachable && reachable) return this
        // Regular merge
        val mergedValue = value.merge(otherInfo.value)
        val mergedState = state.merge(otherInfo.state)
        return UEvaluationInfo(mergedValue, mergedState)
    }

    fun copy(value: UValue) = if (value != this.value) UEvaluationInfo(value, state) else this

    val reachable: Boolean
        get() = value.reachable
}