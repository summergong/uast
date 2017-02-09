package org.jetbrains.uast

import com.intellij.psi.PsiClass
import org.jetbrains.uast.internal.acceptList
import org.jetbrains.uast.internal.log
import org.jetbrains.uast.visitor.UastTypedVisitor
import org.jetbrains.uast.visitor.UastVisitor

/**
 * An annotation wrapper to be used in [UastVisitor].
 */
interface UAnnotation : UElement, UResolvable {
    /**
     * Returns the annotation qualified name.
     */
    val qualifiedName: String?

    /**
     * Returns the annotation class, or null if the class reference was not resolved.
     */
    override fun resolve(): PsiClass?

    /**
     * Returns the annotation values.
     */
    val attributeValues: List<UNamedExpression>

    fun findAttributeValue(name: String?): UExpression?

    fun findDeclaredAttributeValue(name: String?): UExpression?

    override fun asRenderString() = buildString {
        append("@")
        append(qualifiedName)
        if(attributeValues.isNotEmpty()) {
            attributeValues.joinTo(
                    buffer = this,
                    prefix = "(",
                    postfix = ")",
                    transform = UNamedExpression::asRenderString)
        }
    }

    override fun asLogString() = log("fqName = $qualifiedName")

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitAnnotation(this)) return
        attributeValues.acceptList(visitor)
        visitor.afterVisitAnnotation(this)
    }

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) =
            visitor.visitAnnotation(this, data)
}