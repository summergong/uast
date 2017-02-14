package org.jetbrains.uast.java

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import org.jetbrains.uast.*
import org.jetbrains.uast.java.expressions.JavaUNamedExpression

class JavaUAnnotation(
        override val psi: PsiAnnotation,
        override val uastParent: UElement?
) : UAnnotation {
    override val qualifiedName: String?
        get() = psi.qualifiedName

    override val attributeValues: List<UNamedExpression> by lz {
        val context = getUastContext()
        val attributes = psi.parameterList.attributes

        attributes.map { attribute -> JavaUNamedExpression(attribute, this) }
    }

    override fun resolve(): PsiClass? = psi.nameReferenceElement?.resolve() as? PsiClass

    override fun findAttributeValue(name: String?): UExpression? {
        val context = getUastContext()
        val attributeValue = psi.findAttributeValue(name) ?: return null
        return context.convertElement(attributeValue, this, null) as? UExpression ?: UastEmptyExpression
    }

    override fun findDeclaredAttributeValue(name: String?): UExpression? {
        val context = getUastContext()
        val attributeValue = psi.findDeclaredAttributeValue(name) ?: return null
        return context.convertElement(attributeValue, this, null) as? UExpression ?: UastEmptyExpression
    }

    companion object {
        @JvmStatic
        fun wrap(annotation: PsiAnnotation): UAnnotation = JavaUAnnotation(annotation, null)

        @JvmStatic
        fun wrap(annotations: List<PsiAnnotation>): List<UAnnotation> = annotations.map { JavaUAnnotation(it, null) }

        @JvmStatic
        fun wrap(annotations: Array<PsiAnnotation>): List<UAnnotation> = annotations.map { JavaUAnnotation(it, null) }
    }
}