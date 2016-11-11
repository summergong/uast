package org.jetbrains.uast

import org.jetbrains.uast.UExpression

/**
 * Represents expression (break / continue) with label
 */
interface UExpressionWithLabel : UExpression {
    /**
     * Returns the expression label, or null if the label is not specified.
     */
    val label: String?
}
