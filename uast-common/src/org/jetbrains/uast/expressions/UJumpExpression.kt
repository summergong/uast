package org.jetbrains.uast

/**
 * Represents jump expression (break / continue) with label
 */
interface UJumpExpression : UExpression {
    /**
     * Returns the expression label, or null if the label is not specified.
     */
    val label: String?
}
