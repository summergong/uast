package org.jetbrains.uast.evaluation

import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.*
import org.jetbrains.uast.values.UUndeterminedValue
import org.jetbrains.uast.values.UValue

interface UEvaluatorExtension {

    companion object {
        val EXTENSION_POINT_NAME: ExtensionPointName<UEvaluatorExtension> =
                ExtensionPointName.create<UEvaluatorExtension>("org.jetbrains.uast.evaluation.UEvaluatorExtension")
    }

    infix fun UValue.to(state: UEvaluationState) = UEvaluationInfo(this, state)

    val language: Language

    fun evaluatePostfix(
            operator: UastPostfixOperator,
            operandValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state

    fun evaluatePrefix(
            operator: UastPrefixOperator,
            operandValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state

    fun evaluateBinary(
            binaryExpression: UBinaryExpression,
            leftValue: UValue,
            rightValue: UValue,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state

    fun evaluateQualified(
            accessType: UastQualifiedExpressionAccessType,
            receiverInfo: UEvaluationInfo,
            selectorInfo: UEvaluationInfo
    ): UEvaluationInfo = UUndeterminedValue to selectorInfo.state

    fun evaluateMethodCall(
            target: PsiMethod,
            argumentValues: List<UValue>,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state

    fun evaluateVariable(
            variable: UVariable,
            state: UEvaluationState
    ): UEvaluationInfo = UUndeterminedValue to state
}