package org.jetbrains.uast


import org.jetbrains.uast.internal.acceptList
import org.jetbrains.uast.internal.log
import org.jetbrains.uast.visitor.UastTypedVisitor
import org.jetbrains.uast.visitor.UastVisitor

/**
 * Represents a polyadic expression (value1 op value2 op value3 op ...), eg. `2 + "A" + c + d`.
 */
interface UPolyadicExpression : UExpression {

    /**
     * Returns a list of expression operands.
     */
    val operands: List<UExpression>

    /**
     * Returns the operator.
     */
    val operator: UastBinaryOperator

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitPolyadicExpression(this)) return
        annotations.acceptList(visitor)
        operands.acceptList(visitor)
        visitor.afterVisitPolyadicExpression(this)
    }

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) =
            visitor.visitPolyadicExpression(this, data)

    override fun asLogString() = log("operator = $operator")

    override fun asRenderString() =
            operands.joinToString(separator = " ${operator.text} ", transform = UExpression::asRenderString)
}