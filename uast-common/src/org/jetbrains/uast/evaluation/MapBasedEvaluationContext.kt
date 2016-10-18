package org.jetbrains.uast.evaluation

import org.jetbrains.uast.*
import org.jetbrains.uast.values.UValue
import org.jetbrains.uast.visitor.UastVisitor

class MapBasedEvaluationContext(
        override val file: UFile,
        override val uastContext: UastContext
) : UEvaluationContext {
    private val evaluators = mutableMapOf<UMethod, UEvaluator>()

    override fun analyzeAll(state: UEvaluationState): UEvaluationContext {
        file.accept(object: UastVisitor {
            override fun visitElement(node: UElement) = false

            override fun visitMethod(node: UMethod): Boolean {
                analyze(node, state)
                return true
            }
        })
        return this
    }

    override fun analyze(method: UMethod, state: UEvaluationState) =
            createEvaluator(uastContext).apply {
                analyze(method, state)
                evaluators[method] = this
            }

    override fun getEvaluator(method: UMethod) = evaluators[method] ?: analyze(method)

    override fun valueOf(expression: UExpression): UValue {
        val method = expression.getContainingUMethod() ?: return UValue.Undetermined
        val evaluator = getEvaluator(method)
        return evaluator.evaluate(expression)
    }
}