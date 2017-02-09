package org.jetbrains.uast.evaluation

import com.intellij.openapi.extensions.Extensions
import com.intellij.psi.PsiElement
import org.jetbrains.uast.*
import org.jetbrains.uast.values.UDependency
import org.jetbrains.uast.values.UValue

// Role: at the current state, evaluate expression(s)
interface UEvaluator {

    val context: UastContext

    val languageExtensions: List<UEvaluatorExtension>
        get() {
            val rootArea = Extensions.getRootArea()
            if (!rootArea.hasExtensionPoint(UEvaluatorExtension.EXTENSION_POINT_NAME.name)) return listOf()
            return rootArea.getExtensionPoint(UEvaluatorExtension.EXTENSION_POINT_NAME).extensions.toList()
        }

    fun PsiElement.languageExtension() = languageExtensions.firstOrNull { it.language == language }

    fun UElement.languageExtension() = psi?.languageExtension()

    fun analyze(method: UMethod, state: UEvaluationState = method.createEmptyState())

    fun analyze(field: UField, state: UEvaluationState = field.createEmptyState())

    fun evaluate(expression: UExpression, state: UEvaluationState? = null): UValue

    fun getDependents(dependency: UDependency): Set<UValue>
}

fun createEvaluator(context: UastContext, extensions: List<UEvaluatorExtension>): UEvaluator =
        TreeBasedEvaluator(context, extensions)
