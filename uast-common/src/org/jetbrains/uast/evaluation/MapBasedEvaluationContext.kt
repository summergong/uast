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
        })
        return this
    }

    private fun getOrCreateEvaluator(declaration: UDeclaration, state: UEvaluationState? = null) =
            evaluators[declaration] ?: createEvaluator(uastContext).apply {
                if (declaration is UMethod) {
                    this.analyze(declaration, state ?: declaration.createEmptyState())
                }
                evaluators[declaration] = this
            }

    override fun analyze(declaration: UDeclaration, state: UEvaluationState) = getOrCreateEvaluator(declaration, state)

    override fun getEvaluator(declaration: UDeclaration) = getOrCreateEvaluator(declaration)

    override fun valueOf(expression: UExpression): UValue {
        val method = expression.getContainingUMethod() ?: return UValue.Undetermined
        val evaluator = getEvaluator(method)
        return evaluator.evaluate(expression)
    }
}