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

abstract class SimpleEvaluatorExtension : AbstractEvaluatorExtension(Language.ANY) {
    override final fun evaluatePostfix(operator: UastPostfixOperator, operandValue: UValue, state: UEvaluationState): UEvaluationInfo {
        val result = evaluatePostfix(operator, operandValue)
        return if (result != UUndeterminedValue)
            result.toConstant() to state
        else
            super.evaluatePostfix(operator, operandValue, state)
    }

    open fun evaluatePostfix(operator: UastPostfixOperator, operandValue: UValue): Any? = UUndeterminedValue

    override final fun evaluatePrefix(operator: UastPrefixOperator, operandValue: UValue, state: UEvaluationState): UEvaluationInfo {
        val result = evaluatePrefix(operator, operandValue)
        return if (result != UUndeterminedValue)
            result.toConstant() to state
        else
            super.evaluatePrefix(operator, operandValue, state)
    }

    open fun evaluatePrefix(operator: UastPrefixOperator, operandValue: UValue): Any? = UUndeterminedValue

    override final fun evaluateBinary(binaryExpression: UBinaryExpression, leftValue: UValue, rightValue: UValue, state: UEvaluationState): UEvaluationInfo {
        val result = evaluateBinary(binaryExpression, leftValue, rightValue)
        return if (result != UUndeterminedValue)
            result.toConstant() to state
        else
            super.evaluateBinary(binaryExpression, leftValue, rightValue, state)
    }

    open fun evaluateBinary(binaryExpression: UBinaryExpression, leftValue: UValue, rightValue: UValue): Any? = UUndeterminedValue

    override final fun evaluateMethodCall(target: PsiMethod, argumentValues: List<UValue>, state: UEvaluationState): UEvaluationInfo {
        val result = evaluateMethodCall(target, argumentValues)
        return if (result != UUndeterminedValue)
            result.toConstant() to state
        else
            super.evaluateMethodCall(target, argumentValues, state)
    }

    open fun evaluateMethodCall(target: PsiMethod, argumentValues: List<UValue>): Any? = UUndeterminedValue

    override final fun evaluateVariable(variable: UVariable, state: UEvaluationState): UEvaluationInfo {
        val result = evaluateVariable(variable)
        return if (result != UUndeterminedValue)
            result.toConstant() to state
        else
            super.evaluateVariable(variable, state)
    }

    open fun evaluateVariable(variable: UVariable): Any? = UUndeterminedValue
}
