package org.jetbrains.uast.evaluation

import org.jetbrains.uast.*
import org.jetbrains.uast.values.UValue
import org.jetbrains.uast.visitor.UastVisitor
import java.util.*

class MapBasedEvaluationContext(
        override val uastContext: UastContext
) : UEvaluationContext {
    private val evaluators = WeakHashMap<UDeclaration, UEvaluator>()

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
            evaluators[declaration] ?: createEvaluator(uastContext).apply {
                when (declaration) {
                    is UMethod -> this.analyze(declaration, state ?: declaration.createEmptyState())
                    is UField -> this.analyze(declaration, state ?: declaration.createEmptyState())
                }
                evaluators[declaration] = this
            }

    override fun analyze(declaration: UDeclaration, state: UEvaluationState) = getOrCreateEvaluator(declaration, state)

    override fun getEvaluator(declaration: UDeclaration) = getOrCreateEvaluator(declaration)

    private fun getEvaluator(expression: UExpression): UEvaluator? {
        var containingElement = expression.containingElement
        while (containingElement != null) {
            if (containingElement is UDeclaration) {
                val evaluator = evaluators[containingElement]
                if (evaluator != null) {
                    return evaluator
                }
            }
            containingElement = containingElement.containingElement
        }
        return null
    }

    override fun valueOf(expression: UExpression) =
            getEvaluator(expression)?.evaluate(expression) ?: UValue.Undetermined
}