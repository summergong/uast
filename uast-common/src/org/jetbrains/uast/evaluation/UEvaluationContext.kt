package org.jetbrains.uast.evaluation

import org.jetbrains.uast.*
import org.jetbrains.uast.values.UValue

interface UEvaluationContext {
    val file: UFile

    val uastContext: UastContext

    fun analyzeAll(state: UEvaluationState = file.evaluationState()): UEvaluationContext

    fun analyze(method: UMethod, state: UEvaluationState = method.evaluationState()): UEvaluator

    fun valueOf(expression: UExpression): UValue

    fun getEvaluator(method: UMethod): UEvaluator
}

fun UFile.analyzeAll(context: UastContext = getUastContext()): UEvaluationContext =
        MapBasedEvaluationContext(this, context).analyzeAll()