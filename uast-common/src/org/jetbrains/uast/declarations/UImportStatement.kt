package org.jetbrains.uast

import org.jetbrains.uast.internal.log
import org.jetbrains.uast.visitor.UastTypedVisitor
import org.jetbrains.uast.visitor.UastVisitor

/**
 * Represents an import statement.
 */
interface UImportStatement : UResolvable, UElement {
    /**
     * Returns true if the statement is an import-on-demand (star-import) statement.
     */
    val isOnDemand: Boolean

    /**
     * Returns the reference to the imported element.
     */
    val importReference: UElement?
    
    override fun asLogString() = log("isOnDemand = $isOnDemand")

    override fun asRenderString() = "import " + (importReference?.asRenderString() ?: "<error>")

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitImportStatement(this)) return
        visitor.afterVisitImportStatement(this)
    }

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) =
            visitor.visitImportStatement(this, data)
}