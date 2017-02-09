package org.jetbrains.uast.evaluation

import com.intellij.lang.Language
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*
import org.jetbrains.uast.values.UUndeterminedValue
import org.jetbrains.uast.values.UValue

abstract class AbstractEvaluatorExtension(override val language: Language) : UEvaluatorExtension {
    override fun evaluatePostfix(
            operator: UastPostfixOperator,
            operandValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state

    override fun evaluatePrefix(
            operator: UastPrefixOperator,
            operandValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state

    override fun evaluateBinary(
            binaryExpression: UBinaryExpression,
            leftValue: UValue,
            rightValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state

    override fun evaluateQualified(
            accessType: UastQualifiedExpressionAccessType,
            receiverInfo: UEvaluationInfo,
            selectorInfo: UEvaluationInfo
    ): UEvaluationInfo = UUndeterminedValue to selectorInfo.state

    override fun evaluateMethodCall(
            target: PsiMethod,
            argumentValues: List<UValue>,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state

    override fun evaluateVariable(
            variable: UVariable,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state
}
