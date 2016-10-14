package org.jetbrains.uast.kotlin

import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.uast.*

class KotlinUAnnotation(
        override val psi: KtAnnotationEntry,
        override val containingElement: UElement?
) : UAnnotation {
    private val resolvedAnnotation by lz { psi.analyze()[BindingContext.ANNOTATION, psi] }

    override val qualifiedName: String?
        get() = resolvedAnnotation?.type?.constructor?.declarationDescriptor?.fqNameSafe?.asString()

    override val attributeValues by lz {
        val context = getUastContext()
        psi.valueArguments.map { arg ->
            val name = arg.getArgumentName()?.asName?.asString() ?: ""
            UNamedExpression(name, this).apply {
                val value = arg.getArgumentExpression()?.let { context.convertElement(it, this) } as? UExpression
                expression = value ?: UastEmptyExpression
            }
        }
    }

    override fun findDeclaredAttributeValue(name: String?): UNamedExpression? {
        return attributeValues.firstOrNull { it.matchesName(name ?: "value") }
    }
}