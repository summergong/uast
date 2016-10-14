package org.jetbrains.uast

import com.intellij.psi.PsiAnnotation
import org.jetbrains.uast.internal.acceptList
import org.jetbrains.uast.psi.PsiElementBacked
import org.jetbrains.uast.visitor.UastTypedVisitor
import org.jetbrains.uast.visitor.UastVisitor

/**
 * An annotation wrapper to be used in [UastVisitor].
 */
interface UAnnotation : UElement, PsiElementBacked {
    /**
     * Returns the annotation qualified name.
     */
    val qualifiedName: String?

    /**
     * Returns the annotation values.
     */
    val attributeValues: List<UNamedExpression>

    fun findDeclaredAttributeValue(name: String?): UNamedExpression?

    override fun asLogString() = "UAnnotation"

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitAnnotation(this)) return
        attributeValues.acceptList(visitor)
        visitor.afterVisitAnnotation(this)
    }

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) =
            visitor.visitAnnotation(this, data)
}