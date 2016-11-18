package org.jetbrains.uast.evaluation

import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiField
import com.intellij.psi.PsiType
import com.intellij.psi.PsiVariable
import org.jetbrains.uast.*
import org.jetbrains.uast.expressions.UReferenceExpression
import org.jetbrains.uast.values.*
import org.jetbrains.uast.values.UValue.Nothing.JumpKind.BREAK
import org.jetbrains.uast.values.UValue.Nothing.JumpKind.CONTINUE
import org.jetbrains.uast.visitor.UastTypedVisitor

class TreeBasedEvaluator(
        override val context: UastContext
) : UastTypedVisitor<UEvaluationState, UEvaluationInfo>, UEvaluator {

    private val inputStateCache = mutableMapOf<UExpression, UEvaluationState>()

    private val resultCache = mutableMapOf<UExpression, UEvaluationInfo>()

    override fun visitElement(node: UElement, data: UEvaluationState): UEvaluationInfo {
        return UEvaluationInfo(UValue.Undetermined, data).apply {
            if (node is UExpression) {
                this storeResultFor node
            }
        }
    }

    override fun analyze(method: UMethod, state: UEvaluationState) {
        method.uastBody?.accept(this, state)
    }

    override fun analyze(field: UField, state: UEvaluationState) {
        field.uastInitializer?.accept(this, state)
    }

    override fun evaluate(expression: UExpression, state: UEvaluationState?): UValue {
        if (state == null) {
            val result = resultCache[expression]
            if (result != null) return result.value
        }
        val inputState = state ?: inputStateCache[expression] ?: expression.createEmptyState()
        return expression.accept(this, inputState).value
    }

    // ----------------------- //

    private infix fun UValue.to(state: UEvaluationState) = UEvaluationInfo(this, state)

    private infix fun UEvaluationInfo.storeResultFor(expression: UExpression) = apply {
        resultCache[expression] = this
    }

    override fun visitLiteralExpression(node: ULiteralExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val value = node.value
        return when (value) {
            null -> UNullConstant
            is Float -> UFloatConstant.create(value.toDouble(), UNumericType.FLOAT)
            is Double -> UFloatConstant.create(value, UNumericType.DOUBLE)
            is Long -> ULongConstant(value)
            is Int -> UIntConstant(value, UNumericType.INT)
            is Short -> UIntConstant(value.toInt(), UNumericType.SHORT)
            is Byte -> UIntConstant(value.toInt(), UNumericType.BYTE)
            is Char -> UCharConstant(value)
            is Boolean -> UBooleanConstant.valueOf(value)
            is String -> UStringConstant(value)
            else -> UValue.Undetermined
        } to data storeResultFor node
    }

    override fun visitClassLiteralExpression(node: UClassLiteralExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        return (node.type?.let(::UClassConstant) ?: UValue.Undetermined) to data storeResultFor node
    }

    override fun visitReturnExpression(node: UReturnExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val argument = node.returnExpression
        return UValue.Nothing(node) to (argument?.accept(this, data)?.state ?: data) storeResultFor node
    }

    override fun visitBreakExpression(node: UBreakExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        return UValue.Nothing(node) to data storeResultFor node
    }
    override fun visitContinueExpression(node: UContinueExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        return UValue.Nothing(node) to data storeResultFor node
    }

    override fun visitThrowExpression(node: UThrowExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        return UValue.Nothing(node) to data storeResultFor node
    }
    // ----------------------- //

    override fun visitSimpleNameReferenceExpression(
            node: USimpleNameReferenceExpression,
            data: UEvaluationState
    ): UEvaluationInfo {
        inputStateCache[node] = data
        val resolvedElement = node.resolve()
        return when (resolvedElement) {
            is PsiEnumConstant -> UEnumEntryValueConstant(resolvedElement)
            is PsiField -> if (resolvedElement.hasModifierProperty("final")) {
                data[context.getVariable(resolvedElement)]
            }
            else {
                return super.visitSimpleNameReferenceExpression(node, data)
            }
            is PsiVariable -> data[context.getVariable(resolvedElement)]
            else -> return super.visitSimpleNameReferenceExpression(node, data)
        } to data storeResultFor node
    }

    override fun visitReferenceExpression(
            node: UReferenceExpression,
            data: UEvaluationState
    ): UEvaluationInfo {
        inputStateCache[node] = data
        return UValue.CallResult(node) to data storeResultFor node
    }

    // ----------------------- //

    private fun UExpression.assign(
            valueInfo: UEvaluationInfo,
            operator: UastBinaryOperator.AssignOperator = UastBinaryOperator.ASSIGN
    ): UEvaluationInfo {
        this.accept(this@TreeBasedEvaluator, valueInfo.state)
        if (this is UResolvable) {
            val resolvedElement = resolve()
            if (resolvedElement is PsiVariable) {
                val variable = context.getVariable(resolvedElement)
                val currentValue = valueInfo.state[variable]
                val result = when (operator) {
                    UastBinaryOperator.ASSIGN -> valueInfo.value
                    UastBinaryOperator.PLUS_ASSIGN -> currentValue + valueInfo.value
                    UastBinaryOperator.MINUS_ASSIGN -> currentValue - valueInfo.value
                    UastBinaryOperator.MULTIPLY_ASSIGN -> currentValue * valueInfo.value
                    UastBinaryOperator.DIVIDE_ASSIGN -> currentValue / valueInfo.value
                    UastBinaryOperator.REMAINDER_ASSIGN -> currentValue % valueInfo.value
                    UastBinaryOperator.AND_ASSIGN -> currentValue bitwiseAnd valueInfo.value
                    UastBinaryOperator.OR_ASSIGN -> currentValue bitwiseOr valueInfo.value
                    UastBinaryOperator.XOR_ASSIGN -> currentValue bitwiseXor valueInfo.value
                    UastBinaryOperator.SHIFT_LEFT_ASSIGN -> currentValue shl valueInfo.value
                    UastBinaryOperator.SHIFT_RIGHT_ASSIGN -> currentValue shr valueInfo.value
                    UastBinaryOperator.UNSIGNED_SHIFT_RIGHT_ASSIGN -> currentValue ushr valueInfo.value
                    else -> UValue.Undetermined
                }
                return result to valueInfo.state.assign(variable, result, this)
            }
        }
        return UValue.Undetermined to valueInfo.state
    }

    private fun UExpression.assign(
            operator: UastBinaryOperator.AssignOperator,
            value: UExpression,
            data: UEvaluationState
    ) = assign(value.accept(this@TreeBasedEvaluator, data), operator)

    override fun visitPrefixExpression(node: UPrefixExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val operandInfo = node.operand.accept(this, data)
        val operandValue = operandInfo.value
        if (!operandValue.reachable) return operandInfo storeResultFor node
        return when (node.operator) {
            UastPrefixOperator.UNARY_PLUS -> operandValue
            UastPrefixOperator.UNARY_MINUS -> -operandValue
            UastPrefixOperator.LOGICAL_NOT -> !operandValue
            UastPrefixOperator.INC -> {
                val resultValue = operandValue.inc()
                val newState = node.operand.assign(resultValue to operandInfo.state).state
                return resultValue to newState storeResultFor node
            }
            UastPrefixOperator.DEC -> {
                val resultValue = operandValue.dec()
                val newState = node.operand.assign(resultValue to operandInfo.state).state
                return resultValue to newState storeResultFor node
            }
            else -> {
                return node.languageExtension()?.evaluatePrefix(
                        node.operator, operandValue, operandInfo.state
                ) ?: UValue.Undetermined to operandInfo.state storeResultFor node
            }
        } to operandInfo.state storeResultFor node
    }

    override fun visitPostfixExpression(node: UPostfixExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val operandInfo = node.operand.accept(this, data)
        val operandValue = operandInfo.value
        if (!operandValue.reachable) return operandInfo storeResultFor node
        return when (node.operator) {
            UastPostfixOperator.INC -> {
                operandValue to node.operand.assign(operandValue.inc() to operandInfo.state).state
            }
            UastPostfixOperator.DEC -> {
                operandValue to node.operand.assign(operandValue.dec() to operandInfo.state).state
            }
            else -> {
                return node.languageExtension()?.evaluatePostfix(
                        node.operator, operandValue, operandInfo.state
                ) ?: UValue.Undetermined to operandInfo.state storeResultFor node
            }
        } storeResultFor node
    }

    override fun visitBinaryExpression(node: UBinaryExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val operator = node.operator
        if (operator is UastBinaryOperator.AssignOperator) {
            return node.leftOperand.assign(operator, node.rightOperand, data) storeResultFor node
        }
        val leftInfo = node.leftOperand.accept(this, data)
        if (!leftInfo.reachable) return leftInfo storeResultFor node
        val rightInfo = node.rightOperand.accept(this, leftInfo.state)
        return when (operator) {
            UastBinaryOperator.PLUS -> leftInfo.value + rightInfo.value
            UastBinaryOperator.MINUS -> leftInfo.value - rightInfo.value
            UastBinaryOperator.MULTIPLY -> leftInfo.value * rightInfo.value
            UastBinaryOperator.DIV -> leftInfo.value / rightInfo.value
            UastBinaryOperator.MOD -> leftInfo.value % rightInfo.value
            UastBinaryOperator.EQUALS -> leftInfo.value valueEquals rightInfo.value
            UastBinaryOperator.NOT_EQUALS -> leftInfo.value valueNotEquals rightInfo.value
            UastBinaryOperator.IDENTITY_EQUALS -> leftInfo.value identityEquals rightInfo.value
            UastBinaryOperator.IDENTITY_NOT_EQUALS -> leftInfo.value identityNotEquals rightInfo.value
            UastBinaryOperator.GREATER -> leftInfo.value greater rightInfo.value
            UastBinaryOperator.LESS -> leftInfo.value less rightInfo.value
            UastBinaryOperator.GREATER_OR_EQUALS -> leftInfo.value greaterOrEquals rightInfo.value
            UastBinaryOperator.LESS_OR_EQUALS -> leftInfo.value lessOrEquals rightInfo.value
            UastBinaryOperator.LOGICAL_AND -> leftInfo.value and rightInfo.value
            UastBinaryOperator.LOGICAL_OR -> leftInfo.value or rightInfo.value
            UastBinaryOperator.BITWISE_AND -> leftInfo.value bitwiseAnd rightInfo.value
            UastBinaryOperator.BITWISE_OR -> leftInfo.value bitwiseOr rightInfo.value
            UastBinaryOperator.BITWISE_XOR -> leftInfo.value bitwiseXor rightInfo.value
            UastBinaryOperator.SHIFT_LEFT -> leftInfo.value shl rightInfo.value
            UastBinaryOperator.SHIFT_RIGHT -> leftInfo.value shr rightInfo.value
            UastBinaryOperator.UNSIGNED_SHIFT_RIGHT -> leftInfo.value ushr rightInfo.value
            else -> {
                return node.languageExtension()?.evaluateBinary(
                        operator, leftInfo.value, rightInfo.value, rightInfo.state
                ) ?: UValue.Undetermined to rightInfo.state storeResultFor node
            }
        } to rightInfo.state storeResultFor node
    }

    private fun evaluateTypeCast(operandInfo: UEvaluationInfo, type: PsiType): UEvaluationInfo {
        val constant = operandInfo.value.toConstant() ?: return UValue.Undetermined to operandInfo.state
        val resultConstant = when (type) {
            PsiType.BOOLEAN -> {
                constant as? UBooleanConstant
            }
            PsiType.CHAR -> when (constant) {
                // Join the following three in UNumericConstant
                // when https://youtrack.jetbrains.com/issue/KT-14868 is fixed
                is UIntConstant -> UCharConstant(constant.value.toChar())
                is ULongConstant -> UCharConstant(constant.value.toChar())
                is UFloatConstant -> UCharConstant(constant.value.toChar())

                is UCharConstant -> constant
                else -> null
            }
            PsiType.LONG -> {
                (constant as? UNumericConstant)?.value?.toLong()?.let(::ULongConstant)
            }
            PsiType.BYTE, PsiType.SHORT, PsiType.INT -> {
                (constant as? UNumericConstant)?.value?.toInt()?.let { UIntConstant(it, type) }
            }
            PsiType.FLOAT, PsiType.DOUBLE -> {
                (constant as? UNumericConstant)?.value?.toDouble()?.let { UFloatConstant.create(it, type) }
            }
            else -> when (type.name) {
                "java.lang.String" -> UStringConstant(constant.asString())
                else -> null
            }
        } ?: return UValue.Undetermined to operandInfo.state
        return when (operandInfo.value) {
            resultConstant -> return operandInfo
            is UValue.AbstractConstant -> resultConstant
            is UValue.Dependent -> UValue.Dependent.create(resultConstant, operandInfo.value.dependencies)
            else -> UValue.Undetermined
        } to operandInfo.state
    }

    private fun evaluateTypeCheck(operandInfo: UEvaluationInfo, type: PsiType): UEvaluationInfo {
        val constant = operandInfo.value.toConstant() ?: return UValue.Undetermined to operandInfo.state
        val valid = when (type) {
            PsiType.BOOLEAN -> constant is UBooleanConstant
            PsiType.LONG -> constant is ULongConstant
            PsiType.BYTE, PsiType.SHORT, PsiType.INT, PsiType.CHAR -> constant is UIntConstant
            PsiType.FLOAT, PsiType.DOUBLE -> constant is UFloatConstant
            else -> when (type.name) {
                "java.lang.String" -> constant is UStringConstant
                else -> false
            }
        }
        return UBooleanConstant.valueOf(valid) to operandInfo.state
    }

    override fun visitBinaryExpressionWithType(
            node: UBinaryExpressionWithType, data: UEvaluationState
    ): UEvaluationInfo {
        inputStateCache[node] = data
        val operandInfo = node.operand.accept(this, data)
        if (!operandInfo.reachable || operandInfo.value == UValue.Undetermined) {
            return operandInfo storeResultFor node
        }
        return when (node.operationKind) {
            UastBinaryExpressionWithTypeKind.TYPE_CAST -> evaluateTypeCast(operandInfo, node.type)
            UastBinaryExpressionWithTypeKind.INSTANCE_CHECK -> evaluateTypeCheck(operandInfo, node.type)
            else -> UValue.Undetermined to operandInfo.state
        } storeResultFor node
    }

    override fun visitParenthesizedExpression(node: UParenthesizedExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        return node.expression.accept(this, data) storeResultFor node
    }

    override fun visitLabeledExpression(node: ULabeledExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        return node.expression.accept(this, data) storeResultFor node
    }

    override fun visitCallExpression(node: UCallExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data

        var currentInfo = UValue.Undetermined to data
        currentInfo = node.receiver?.accept(this, currentInfo.state) ?: currentInfo
        if (!currentInfo.reachable) return currentInfo storeResultFor node
        for (valueArgument in node.valueArguments) {
            currentInfo = valueArgument.accept(this, currentInfo.state)
            if (!currentInfo.reachable) return currentInfo storeResultFor node
        }

        return UValue.CallResult(node) to currentInfo.state storeResultFor node
    }

    override fun visitQualifiedReferenceExpression(
            node: UQualifiedReferenceExpression,
            data: UEvaluationState
    ): UEvaluationInfo {
        inputStateCache[node] = data

        var currentInfo = UValue.Undetermined to data
        currentInfo = node.receiver.accept(this, currentInfo.state)
        if (!currentInfo.reachable) return currentInfo storeResultFor node

        val selectorInfo = node.selector.accept(this, currentInfo.state)
        return when (node.accessType) {
            UastQualifiedExpressionAccessType.SIMPLE -> {
                selectorInfo
            }
            else -> {
                return node.languageExtension()?.evaluateQualified(
                        node.accessType, currentInfo, selectorInfo
                ) ?: UValue.Undetermined to selectorInfo.state storeResultFor node
            }
        } storeResultFor node
    }

    override fun visitDeclarationsExpression(
            node: UVariableDeclarationsExpression,
            data: UEvaluationState
    ): UEvaluationInfo {
        inputStateCache[node] = data
        var currentInfo = UValue.Undetermined to data
        for (variable in node.variables) {
            currentInfo = variable.accept(this, currentInfo.state)
            if (!currentInfo.reachable) return currentInfo storeResultFor node
        }
        return currentInfo storeResultFor node
    }

    override fun visitVariable(node: UVariable, data: UEvaluationState): UEvaluationInfo {
        val initializer = node.uastInitializer
        val initializerInfo = initializer?.accept(this, data) ?: UValue.Undetermined to data
        if (!initializerInfo.reachable) return initializerInfo
        return UValue.Undetermined to initializerInfo.state.assign(node, initializerInfo.value, node)
    }

    // ----------------------- //

    override fun visitBlockExpression(node: UBlockExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        var currentInfo = UValue.Undetermined to data
        for (expression in node.expressions) {
            currentInfo = expression.accept(this, currentInfo.state)
            if (!currentInfo.reachable) return currentInfo storeResultFor node
        }
        return currentInfo storeResultFor node
    }

    override fun visitIfExpression(node: UIfExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val conditionInfo = node.condition.accept(this, data)
        if (!conditionInfo.reachable) return conditionInfo storeResultFor node

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
            else -> when {
                thenInfo == null -> elseInfo?.merge(defaultInfo) ?: defaultInfo
                elseInfo == null -> thenInfo.merge(defaultInfo)
                else -> thenInfo.merge(elseInfo)
            }
        } storeResultFor node
    }

    override fun visitSwitchExpression(node: USwitchExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val subjectInfo = node.expression?.accept(this, data) ?: UValue.Undetermined to data
        if (!subjectInfo.reachable) return subjectInfo storeResultFor node

        var resultInfo: UEvaluationInfo? = null
        var clauseInfo = subjectInfo
        var fallThroughCondition: UValue = UBooleanConstant.False

        fun List<UExpression>.evaluateAndFold(): UValue =
                this.map {
                    clauseInfo = it.accept(this@TreeBasedEvaluator, clauseInfo.state)
                    (clauseInfo.value valueEquals subjectInfo.value).toConstant() as? UValue ?: UValue.Undetermined
                }.fold(UBooleanConstant.False) { previous: UValue, next -> previous or next }

        clausesLoop@ for (expression in node.body.expressions) {
            val switchClauseWithBody = expression as USwitchClauseExpressionWithBody
            val caseCondition = switchClauseWithBody.caseValues.evaluateAndFold().or(fallThroughCondition)

            if (caseCondition != UBooleanConstant.False) {
                for (bodyExpression in switchClauseWithBody.body.expressions) {
                    clauseInfo = bodyExpression.accept(this, clauseInfo.state)
                    if (!clauseInfo.reachable) break
                }
                val clauseValue = clauseInfo.value
                if (clauseValue is UValue.Nothing && clauseValue.containingLoopOrSwitch == node) {
                    // break from switch
                    resultInfo = resultInfo?.merge(clauseInfo) ?: clauseInfo
                    if (caseCondition == UBooleanConstant.True) break@clausesLoop
                    clauseInfo = subjectInfo
                    fallThroughCondition = UBooleanConstant.False
                }
                // TODO: jump out
                else {
                    fallThroughCondition = caseCondition
                    clauseInfo = clauseInfo.merge(subjectInfo)
                }
            }
        }

        resultInfo = resultInfo ?: subjectInfo
        val resultValue = resultInfo.value
        if (resultValue is UValue.Nothing && resultValue.containingLoopOrSwitch == node) {
            resultInfo = resultInfo.copy(UValue.Undetermined)
        }
        return resultInfo storeResultFor node
    }

    private fun evaluateLoop(
            loop: ULoopExpression,
            inputState: UEvaluationState,
            condition: UExpression? = null,
            infinite: Boolean = false,
            update: UExpression? = null
    ): UEvaluationInfo {

        fun evaluateCondition(inputState: UEvaluationState): UEvaluationInfo =
                condition?.accept(this, inputState)
                ?: (if (infinite) UBooleanConstant.True else UValue.Undetermined) to inputState

        var resultInfo = UValue.Undetermined to inputState
        do {
            val previousInfo = resultInfo
            resultInfo = evaluateCondition(resultInfo.state)
            val conditionConstant = resultInfo.value.toConstant()
            if (conditionConstant == UBooleanConstant.False) {
                return resultInfo.copy(UValue.Undetermined) storeResultFor loop
            }
            val bodyInfo = loop.body.accept(this, resultInfo.state)
            val bodyValue = bodyInfo.value
            if (bodyValue is UValue.Nothing) {
                if (bodyValue.kind == BREAK && bodyValue.containingLoopOrSwitch == loop) {
                    return if (conditionConstant == UBooleanConstant.True) {
                        bodyInfo.copy(UValue.Undetermined)
                    }
                    else {
                        bodyInfo.copy(UValue.Undetermined).merge(previousInfo)
                    } storeResultFor loop
                }
                else if (bodyValue.kind == CONTINUE && bodyValue.containingLoopOrSwitch == loop) {
                    val updateInfo = update?.accept(this, bodyInfo.state) ?: bodyInfo
                    resultInfo = updateInfo.copy(UValue.Undetermined).merge(previousInfo)
                }
                else {
                    return if (conditionConstant == UBooleanConstant.True) {
                        bodyInfo
                    }
                    else {
                        resultInfo.copy(UValue.Undetermined)
                    } storeResultFor loop
                }
            }
            else {
                val updateInfo = update?.accept(this, bodyInfo.state) ?: bodyInfo
                resultInfo = updateInfo.merge(previousInfo)
            }
        } while (previousInfo != resultInfo)
        return resultInfo.copy(UValue.Undetermined) storeResultFor loop
    }

    override fun visitForEachExpression(node: UForEachExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val iterableInfo = node.iteratedValue.accept(this, data)
        return evaluateLoop(node, iterableInfo.state)
    }

    override fun visitForExpression(node: UForExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val initialState = node.declaration?.accept(this, data)?.state ?: data
        return evaluateLoop(node, initialState, node.condition, node.condition == null, node.update)
    }

    override fun visitWhileExpression(node: UWhileExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        return evaluateLoop(node, data, node.condition)
    }

    override fun visitDoWhileExpression(node: UDoWhileExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val bodyInfo = node.body.accept(this, data)
        return evaluateLoop(node, bodyInfo.state, node.condition)
    }

    override fun visitTryExpression(node: UTryExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val tryInfo = node.tryClause.accept(this, data)
        val mergedTryInfo = tryInfo.merge(UValue.Undetermined to data)
        val catchInfoList = node.catchClauses.map { it.accept(this, mergedTryInfo.state) }
        val mergedTryCatchInfo = catchInfoList.fold(mergedTryInfo, UEvaluationInfo::merge)
        val finallyInfo = node.finallyClause?.accept(this, mergedTryCatchInfo.state) ?: mergedTryCatchInfo
        return finallyInfo storeResultFor node
    }

    // ----------------------- //

    override fun visitObjectLiteralExpression(node: UObjectLiteralExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val objectInfo = node.declaration.accept(this, data)
        val resultState = data.merge(objectInfo.state)
        return UValue.Undetermined to resultState storeResultFor node
    }

    override fun visitLambdaExpression(node: ULambdaExpression, data: UEvaluationState): UEvaluationInfo {
        inputStateCache[node] = data
        val lambdaInfo = node.body.accept(this, data)
        val resultState = data.merge(lambdaInfo.state)
        return UValue.Undetermined to resultState storeResultFor node
    }

    override fun visitClass(node: UClass, data: UEvaluationState): UEvaluationInfo {
        // fields / initializers / nested classes?
        var resultState = data
        for (method in node.uastMethods) {
            resultState = resultState.merge(method.accept(this, resultState).state)
        }
        return UValue.Undetermined to resultState
    }

    override fun visitMethod(node: UMethod, data: UEvaluationState): UEvaluationInfo {
        return UValue.Undetermined to (node.uastBody?.accept(this, data)?.state ?: data)
    }
}