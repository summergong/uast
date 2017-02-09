package org.jetbrains.uast.evaluation

import com.intellij.openapi.util.Key
import org.jetbrains.uast.*
import org.jetbrains.uast.values.UValue
import java.lang.ref.SoftReference

interface UEvaluationContext {
    val uastContext: UastContext

    val extensions: List<UEvaluatorExtension>

    fun analyzeAll(file: UFile, state: UEvaluationState = file.createEmptyState()): UEvaluationContext

    fun analyze(declaration: UDeclaration, state: UEvaluationState = declaration.createEmptyState()): UEvaluator

    fun valueOf(expression: UExpression): UValue

    fun getEvaluator(declaration: UDeclaration): UEvaluator
}

fun UFile.analyzeAll(context: UastContext = getUastContext(), extensions: List<UEvaluatorExtension> = emptyList()): UEvaluationContext =
        MapBasedEvaluationContext(context, extensions).analyzeAll(this)

@JvmOverloads
fun UExpression.uValueOf(extensions: List<UEvaluatorExtension> = emptyList()): UValue? {
    val declaration = getContainingDeclaration() ?: return null
    val context = declaration.getEvaluationContextWithCaching(extensions)
    context.analyze(declaration)
    return context.valueOf(this)
}

fun UExpression.uValueOf(vararg extensions: UEvaluatorExtension): UValue? = uValueOf(extensions.asList())

fun UDeclaration.getEvaluationContextWithCaching(extensions: List<UEvaluatorExtension> = emptyList()): UEvaluationContext {
    return containingFile?.let { file ->
        val cachedContext = file.getUserData(EVALUATION_CONTEXT_KEY)?.get()
        if (cachedContext != null && cachedContext.extensions == extensions)
            cachedContext
        else
            MapBasedEvaluationContext(getUastContext(), extensions).apply {
                file.putUserData(EVALUATION_CONTEXT_KEY, SoftReference(this))
            }

    } ?: MapBasedEvaluationContext(getUastContext(), extensions)
}

val EVALUATION_CONTEXT_KEY = Key<SoftReference<out UEvaluationContext>>("uast.EvaluationContext")
