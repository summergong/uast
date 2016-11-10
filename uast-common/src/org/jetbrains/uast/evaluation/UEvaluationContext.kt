package org.jetbrains.uast.evaluation

import org.jetbrains.uast.*
import org.jetbrains.uast.values.UValue

interface UEvaluationContext {
    val uastContext: UastContext

    fun analyzeAll(file: UFile, state: UEvaluationState = file.createEmptyState()): UEvaluationContext

    fun analyze(declaration: UDeclaration, state: UEvaluationState = declaration.createEmptyState()): UEvaluator

    fun valueOf(expression: UExpression): UValue

    fun getEvaluator(declaration: UDeclaration): UEvaluator
}

fun UFile.analyzeAll(context: UastContext = getUastContext()): UEvaluationContext =
        MapBasedEvaluationContext(context).analyzeAll(this)