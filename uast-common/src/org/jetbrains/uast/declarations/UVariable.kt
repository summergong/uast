package org.jetbrains.uast

import com.intellij.psi.*
import org.jetbrains.uast.expressions.UTypeReferenceExpression
import org.jetbrains.uast.internal.acceptList
import org.jetbrains.uast.internal.log
import org.jetbrains.uast.visitor.UastTypedVisitor
import org.jetbrains.uast.visitor.UastVisitor

/**
 * A variable wrapper to be used in [UastVisitor].
 */
interface UVariable : UDeclaration, PsiVariable {
    override val psi: PsiVariable

    /**
     * Returns the variable initializer or the parameter default value, or null if the variable has not an initializer.
     */
    val uastInitializer: UExpression?

    /**
     * Returns variable type reference.
     */
    val typeReference: UTypeReferenceExpression?

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitVariable(this)) return
        annotations.acceptList(visitor)
        uastInitializer?.accept(visitor)
        visitor.afterVisitVariable(this)
    }

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) =
            visitor.visitVariable(this, data)

    @Deprecated("Use uastInitializer instead.", ReplaceWith("uastInitializer"))
    override fun getInitializer() = psi.initializer

    override fun asOwnLogString() = "UVariable (name = $name)"

    override fun asLogString() = log(asOwnLogString(), annotations, uastInitializer)

    override fun asRenderString() = buildString {
        val modifiers = PsiModifier.MODIFIERS.filter { psi.hasModifierProperty(it) }.joinToString(" ")
        if (modifiers.isNotEmpty()) append(modifiers).append(' ')
        append("var ").append(psi.name).append(": ").append(psi.type.getCanonicalText(false))
    }
}

interface UParameter : UVariable, PsiParameter {
    override val psi: PsiParameter

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) = visitor.visitParameter(this, data)
}

interface UField : UVariable, PsiField {
    override val psi: PsiField

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) = visitor.visitField(this, data)
}

interface ULocalVariable : UVariable, PsiLocalVariable {
    override val psi: PsiLocalVariable

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) = visitor.visitLocalVariable(this, data)
}

interface UEnumConstant : UField, UCallExpression, PsiEnumConstant {
    override val psi: PsiEnumConstant

    override fun asOwnLogString() = "UEnumConstant (name = ${psi.name}"

    override fun asLogString() = super<UField>.asLogString()

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitVariable(this)) return
        annotations.acceptList(visitor)
        methodIdentifier?.accept(visitor)
        classReference?.accept(visitor)
        valueArguments.acceptList(visitor)
        visitor.afterVisitVariable(this)
    }

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) =
            visitor.visitEnumConstantExpression(this, data)

    override fun asRenderString() = name ?: "<ERROR>"
}