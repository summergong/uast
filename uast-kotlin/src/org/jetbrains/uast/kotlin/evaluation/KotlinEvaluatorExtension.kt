package org.jetbrains.uast.kotlin.evaluation

import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.uast.UastPostfixOperator
import org.jetbrains.uast.evaluation.UEvaluationInfo
import org.jetbrains.uast.evaluation.UEvaluationState
import org.jetbrains.uast.evaluation.UEvaluatorExtension
import org.jetbrains.uast.kotlin.KotlinPostfixOperators
import org.jetbrains.uast.values.UConstant
import org.jetbrains.uast.values.UNullConstant
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
}