package org.jetbrains.uast.java

import com.intellij.psi.PsiAnnotation
import org.jetbrains.uast.*

class JavaUAnnotation(
        override val psi: PsiAnnotation,
        override val containingElement: UElement?
) : UAnnotation {
    override val qualifiedName: String?
        get() = psi.qualifiedName

    override val attributeValues: List<UNamedExpression> by lz {
        val context = getUastContext()
        val attributes = psi.parameterList.attributes

        attributes.map { attribute ->
            UNamedExpression(attribute.name ?: "", this).apply {
                val value = attribute.value?.let { context.convertElement(it, this, null) } as? UExpression
                expression = value ?: UastEmptyExpression
            }
        }
    }

    fun findAttributeValue(name: String?): UNamedExpression? {
        val context = getUastContext()
        val attributeValue = psi.findAttributeValue(name) ?: return null
        val value = context.convertElement(attributeValue, this, null)
        return UNamedExpression(name ?: "", value)
    }

    override fun findDeclaredAttributeValue(name: String?): UNamedExpression? {
        val context = getUastContext()
        val attributeValue = psi.findDeclaredAttributeValue(name) ?: return null
        val value = context.convertElement(attributeValue, this, null)
        return UNamedExpression(name ?: "", value)
    }
}