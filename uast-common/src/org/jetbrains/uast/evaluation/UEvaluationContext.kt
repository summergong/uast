package org.jetbrains.uast.evaluation

import com.intellij.openapi.util.Key
import org.jetbrains.uast.*
import org.jetbrains.uast.values.UValue
import java.lang.ref.SoftReference

interface UEvaluationContext {
    val uastContext: UastContext

    fun analyzeAll(file: UFile, state: UEvaluationState = file.createEmptyState()): UEvaluationContext

    fun analyze(declaration: UDeclaration, state: UEvaluationState = declaration.createEmptyState()): UEvaluator

    fun valueOf(expression: UExpression): UValue

    fun getEvaluator(declaration: UDeclaration): UEvaluator
}

fun UFile.analyzeAll(context: UastContext = getUastContext()): UEvaluationContext =
        MapBasedEvaluationContext(context).analyzeAll(this)

fun UExpression.uValueOf(): UValue? {
    val declaration = getContainingDeclaration() ?: return null
    val context = declaration.getEvaluationContextWithCaching()
    context.analyze(declaration)
    return context.valueOf(this)
}

fun UDeclaration.getEvaluationContextWithCaching(): UEvaluationContext {
    return containingFile?.let { file ->
        file.getUserData(EVALUATION_CONTEXT_KEY)?.get() ?:
                MapBasedEvaluationContext(getUastContext()).apply {
                    file.putUserData(EVALUATION_CONTEXT_KEY, SoftReference(this))
                }

    } ?: MapBasedEvaluationContext(getUastContext())
}

val EVALUATION_CONTEXT_KEY = Key<SoftReference<out UEvaluationContext>>("uast.EvaluationContext")
