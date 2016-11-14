package org.jetbrains.uast.kotlin.evaluation

import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.uast.UastBinaryOperator
import org.jetbrains.uast.UastPostfixOperator
import org.jetbrains.uast.evaluation.UEvaluationInfo
import org.jetbrains.uast.evaluation.UEvaluationState
import org.jetbrains.uast.evaluation.UEvaluatorExtension
import org.jetbrains.uast.kotlin.KotlinBinaryOperators
import org.jetbrains.uast.kotlin.KotlinPostfixOperators
import org.jetbrains.uast.values.UConstant
import org.jetbrains.uast.values.UNullConstant
import org.jetbrains.uast.values.URangeConstant
import org.jetbrains.uast.values.UValue

class KotlinEvaluatorExtension : UEvaluatorExtension {
    override val language: KotlinLanguage = KotlinLanguage.INSTANCE

    override fun evaluatePostfix(
            operator: UastPostfixOperator,
            operandValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo {
        return when (operator) {
            KotlinPostfixOperators.EXCLEXCL -> when (operandValue.toConstant()) {
                UNullConstant -> UValue.Nothing(null)
                is UConstant -> operandValue
                else -> UValue.Undetermined
            } to state
            else -> return super.evaluatePostfix(operator, operandValue, state)
        }
    }

    private fun UValue.contains(value: UValue): UValue {
        val range = (this as? URangeConstant)?.value ?: return UValue.Undetermined
        return (value greaterOrEquals range.from) and (value lessOrEquals range.to)
    }

    override fun evaluateBinary(
            operator: UastBinaryOperator,
            leftValue: UValue,
            rightValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo {
        return when (operator) {
            KotlinBinaryOperators.IN -> rightValue.contains(leftValue)
            KotlinBinaryOperators.NOT_IN -> !rightValue.contains(leftValue)
            KotlinBinaryOperators.RANGE_TO -> URangeConstant(leftValue, rightValue)
            else -> UValue.Undetermined
        } to state
    }
}