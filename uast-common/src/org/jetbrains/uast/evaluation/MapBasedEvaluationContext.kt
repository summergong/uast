package org.jetbrains.uast.evaluation

import org.jetbrains.uast.*
import org.jetbrains.uast.values.UUndeterminedValue
import org.jetbrains.uast.visitor.UastVisitor
import java.lang.ref.SoftReference
import java.util.*

class MapBasedEvaluationContext(
        override val uastContext: UastContext,
        override val extensions: List<UEvaluatorExtension>
) : UEvaluationContext {
    private val evaluators = WeakHashMap<UDeclaration, SoftReference<UEvaluator>>()

    override fun analyzeAll(file: UFile, state: UEvaluationState): UEvaluationContext {
        file.accept(object: UastVisitor {
            override fun visitElement(node: UElement) = false

            override fun visitMethod(node: UMethod): Boolean {
                analyze(node, state)
                return true
            }

            override fun visitVariable(node: UVariable): Boolean {
                if (node is UField) {
                    analyze(node, state)
                    return true
                }
                else return false
            }
        })
        return this
    }

    private fun getOrCreateEvaluator(declaration: UDeclaration, state: UEvaluationState? = null) =
            evaluators[declaration]?.get() ?: createEvaluator(uastContext, extensions).apply {
                when (declaration) {
                    is UMethod -> this.analyze(declaration, state ?: declaration.createEmptyState())
                    is UField -> this.analyze(declaration, state ?: declaration.createEmptyState())
                }
                evaluators[declaration] = SoftReference(this)
            }

    override fun analyze(declaration: UDeclaration, state: UEvaluationState) = getOrCreateEvaluator(declaration, state)

    override fun getEvaluator(declaration: UDeclaration) = getOrCreateEvaluator(declaration)

    private fun getEvaluator(expression: UExpression): UEvaluator? {
        var containingElement = expression.uastParent
        while (containingElement != null) {
            if (containingElement is UDeclaration) {
                val evaluator = evaluators[containingElement]?.get()
                if (evaluator != null) {
                    return evaluator
                }
            }
            containingElement = containingElement.uastParent
        }
        return null
    }

    override fun valueOf(expression: UExpression) =
            getEvaluator(expression)?.evaluate(expression) ?: UUndeterminedValue
}