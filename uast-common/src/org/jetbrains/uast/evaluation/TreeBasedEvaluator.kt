package org.jetbrains.uast.evaluation

import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiVariable
import org.jetbrains.uast.*
import org.jetbrains.uast.expressions.UReferenceExpression
import org.jetbrains.uast.values.*
import org.jetbrains.uast.visitor.UastTypedVisitor

class TreeBasedEvaluator(
        private val context: UastContext
) : UastTypedVisitor<UEvaluationState, UEvaluationInfo>, UEvaluator {

    private val stateCache = mutableMapOf<UExpression, UEvaluationState>()

    private val resultCache = mutableMapOf<UExpression, UEvaluationInfo>()

    override fun visitElement(node: UElement, data: UEvaluationState): UEvaluationInfo {
        return UEvaluationInfo(UValue.Undetermined, data).apply {
            if (node is UExpression) {
                this storeFor node
            }
        }
    }

    override fun analyze(method: UMethod, state: UEvaluationState) {
        method.uastBody?.accept(this, state)
    }

    override fun evaluate(expression: UExpression, state: UEvaluationState?): UValue {
        if (state == null) {
            val result = resultCache[expression]
            if (result != null) return result.value
        }
        val inputState = state ?: stateCache[expression] ?: expression.createEmptyState()
        return expression.accept(this, inputState).value
    }

    // ----------------------- //

    private infix fun UValue.to(state: UEvaluationState) = UEvaluationInfo(this, state)

    private infix fun UEvaluationInfo.storeFor(expression: UExpression) = apply {
        resultCache[expression] = this
    }

    override fun visitLiteralExpression(node: ULiteralExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        val value = node.value
        return when (value) {
            null -> UNullConstant
            is Number -> value.let {
                if (it is Float || it is Double) UFloatConstant(value.toDouble())
                else UIntConstant(value.toLong())
            }
            is Char -> UIntConstant(value.toLong(), 2)
            is Boolean -> UBooleanConstant.valueOf(value)
            is String -> UStringConstant(value)
            else -> UValue.Undetermined
        } to data storeFor node
    }

    override fun visitClassLiteralExpression(node: UClassLiteralExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        return (node.type?.let(::UClassConstant) ?: UValue.Undetermined) to data storeFor node
    }

    override fun visitReturnExpression(node: UReturnExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        val argument = node.returnExpression
        return UValue.Nothing to (argument?.accept(this, data)?.state ?: data) storeFor node
    }

    override fun visitBreakExpression(node: UBreakExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        return UValue.Nothing to data storeFor node
    }
    override fun visitContinueExpression(node: UContinueExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        return UValue.Nothing to data storeFor node
    }

    override fun visitThrowExpression(node: UThrowExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        return UValue.Nothing to data storeFor node
    }
    // ----------------------- //

    override fun visitSimpleNameReferenceExpression(
            node: USimpleNameReferenceExpression,
            data: UEvaluationState
    ): UEvaluationInfo {
        stateCache[node] = data
        val resolvedElement = node.resolve()
        return when (resolvedElement) {
            is PsiEnumConstant -> UEnumEntryValueConstant(resolvedElement)
            is PsiVariable -> data[context.getVariable(resolvedElement)]
            else -> return visitReferenceExpression(node, data)
        } to data storeFor node
    }

    override fun visitReferenceExpression(
            node: UReferenceExpression,
            data: UEvaluationState
    ): UEvaluationInfo {
        stateCache[node] = data
        return UValue.External(node) to data storeFor node
    }

    // ----------------------- //

    private fun UExpression.assign(value: UExpression, data: UEvaluationState): UEvaluationInfo {
        val valueInfo = value.accept(this@TreeBasedEvaluator, data)
        if (this is UResolvable) {
            val resolvedElement = resolve()
            if (resolvedElement is PsiVariable) {
                val variable = context.getVariable(resolvedElement)
                return UValue.Undetermined to valueInfo.state.assign(variable, valueInfo.value, this)
            }
        }
        return UValue.Undetermined to valueInfo.state
    }

    override fun visitPrefixExpression(node: UPrefixExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        val operandInfo = node.operand.accept(this, data)
        if (operandInfo.value == UValue.Nothing) return operandInfo storeFor node
        return when (node.operator) {
            UastPrefixOperator.UNARY_PLUS -> operandInfo.value
            UastPrefixOperator.UNARY_MINUS -> -operandInfo.value
            UastPrefixOperator.LOGICAL_NOT -> !operandInfo.value
            else -> UValue.Undetermined
        } to operandInfo.state storeFor node
    }

    override fun visitPostfixExpression(node: UPostfixExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        val operandInfo = node.operand.accept(this, data)
        if (operandInfo.value == UValue.Nothing) return operandInfo storeFor node
        return when (node.operator) {
            else -> UValue.Undetermined
        } to operandInfo.state storeFor node
    }

    override fun visitBinaryExpression(node: UBinaryExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        if (node.operator == UastBinaryOperator.ASSIGN) {
            return node.leftOperand.assign(node.rightOperand, data) storeFor node
        }
        val leftInfo = node.leftOperand.accept(this, data)
        if (leftInfo.value == UValue.Nothing) return leftInfo storeFor node
        val rightInfo = node.rightOperand.accept(this, leftInfo.state)
        return when (node.operator) {
            UastBinaryOperator.PLUS -> leftInfo.value + rightInfo.value
            UastBinaryOperator.MINUS -> leftInfo.value - rightInfo.value
            UastBinaryOperator.EQUALS -> leftInfo.value same rightInfo.value
            UastBinaryOperator.NOT_EQUALS -> leftInfo.value notSame rightInfo.value
            UastBinaryOperator.IDENTITY_EQUALS -> leftInfo.value identitySame rightInfo.value
            UastBinaryOperator.IDENTITY_NOT_EQUALS -> leftInfo.value identityNotSame rightInfo.value
            UastBinaryOperator.GREATER -> leftInfo.value greater rightInfo.value
            UastBinaryOperator.LESS -> leftInfo.value less rightInfo.value
            UastBinaryOperator.GREATER_OR_EQUALS -> leftInfo.value greaterOrEquals rightInfo.value
            UastBinaryOperator.LESS_OR_EQUALS -> leftInfo.value lessOrEquals rightInfo.value
            else -> UValue.Undetermined
        } to rightInfo.state storeFor node
    }

    override fun visitDeclarationsExpression(
            node: UVariableDeclarationsExpression,
            data: UEvaluationState
    ): UEvaluationInfo {
        stateCache[node] = data
        var currentInfo = UValue.Undetermined to data
        for (variable in node.variables) {
            currentInfo = variable.accept(this, currentInfo.state)
        }
        return currentInfo storeFor node
    }

    override fun visitVariable(node: UVariable, data: UEvaluationState): UEvaluationInfo {
        val initializer = node.uastInitializer
        val initializerInfo = initializer?.accept(this, data) ?: UValue.Undetermined to data
        return UValue.Undetermined to initializerInfo.state.assign(node, initializerInfo.value, node)
    }

    // ----------------------- //

    override fun visitBlockExpression(node: UBlockExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        var currentInfo = UValue.Undetermined to data
        for (expression in node.expressions) {
            currentInfo = expression.accept(this, currentInfo.state)
        }
        return currentInfo storeFor node
    }

    override fun visitIfExpression(node: UIfExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        val conditionInfo = node.condition.accept(this, data)
        val thenInfo = node.thenExpression?.accept(this, conditionInfo.state)
        val elseInfo = node.elseExpression?.accept(this, conditionInfo.state)
        val conditionValue = conditionInfo.value
        val defaultInfo = UValue.Undetermined to conditionInfo.state
        val constantConditionValue = conditionValue.toConstant()
        return when (constantConditionValue) {
            is UBooleanConstant -> {
                if (constantConditionValue.value) thenInfo ?: defaultInfo
                else elseInfo ?: defaultInfo
            }
            else -> {
                if (thenInfo == null) elseInfo?.merge(defaultInfo) ?: defaultInfo
                else if (elseInfo == null) thenInfo.merge(defaultInfo)
                else thenInfo.merge(elseInfo)
            }
        } storeFor node
    }

    override fun visitSwitchExpression(node: USwitchExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        val subjectInfo = node.expression?.accept(this, data) ?: UValue.Undetermined to data
        val subjectValue = subjectInfo.value
        var resultInfo: UEvaluationInfo? = null
        val switchList = node.body
        for (expression in switchList.expressions) {
            val switchClauseWithBody = expression as USwitchClauseExpressionWithBody
            var clauseInfo = subjectInfo
            val caseValueComparisons = switchClauseWithBody.caseValues.map {
                clauseInfo = it.accept(this, clauseInfo.state)
                (clauseInfo.value same subjectValue).toConstant()
            }
            val mustBeTrue = UBooleanConstant.True in caseValueComparisons
            val canBeTrue = mustBeTrue || null in caseValueComparisons
            if (canBeTrue) {
                for (bodyExpression in switchClauseWithBody.body.expressions) {
                    clauseInfo = bodyExpression.accept(this, clauseInfo.state)
                }
                resultInfo = resultInfo?.merge(clauseInfo) ?: clauseInfo
                if (mustBeTrue) break
            }
        }
        return (resultInfo ?: subjectInfo) storeFor node
    }

    private fun evaluateLoop(loop: ULoopExpression, inputState: UEvaluationState): UEvaluationInfo {
        var resultInfo = UValue.Undetermined to inputState
        do {
            val bodyInfo = loop.body.accept(this, resultInfo.state)
            val previousInfo = resultInfo
            resultInfo = bodyInfo.merge(previousInfo)
        } while (previousInfo != resultInfo)
        return resultInfo.changeValue(UValue.Undetermined) storeFor loop
    }

    override fun visitForExpression(node: UForExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        val initialInfo = node.declaration?.accept(this, data) ?: UValue.Undetermined to data
        val resultInfo = node.condition?.accept(this, initialInfo.state) ?: UBooleanConstant.True to data
        val conditionConstant = resultInfo.value.toConstant()
        if (conditionConstant == UBooleanConstant.False) {
            return resultInfo.changeValue(UValue.Undetermined) storeFor node
        }
        return evaluateLoop(node, resultInfo.state)
    }

    override fun visitForEachExpression(node: UForEachExpression, data: UEvaluationState): UEvaluationInfo {
        stateCache[node] = data
        return evaluateLoop(node, data)
    }
}