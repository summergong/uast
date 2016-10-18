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

    override fun asLogString() = log("name = $name")

    override fun asRenderString() = buildString {
        append(psi.renderModifiers())
        append("var ").append(psi.name).append(": ").append(psi.type.getCanonicalText(false))
        uastInitializer?.let { initializer -> append(" = " + initializer.asRenderString()) }
    }
}

interface UParameter : UVariable, PsiParameter {
    override val psi: PsiParameter

    override fun asLogString() = log("name = $name")

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) = visitor.visitParameter(this, data)
}

interface UField : UVariable, PsiField {
    override val psi: PsiField

    override fun asLogString() = log("name = $name")

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) = visitor.visitField(this, data)
}

interface ULocalVariable : UVariable, PsiLocalVariable {
    override val psi: PsiLocalVariable

    override fun asLogString() = log("name = $name")

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) = visitor.visitLocalVariable(this, data)
}

interface UEnumConstant : UField, UCallExpression, PsiEnumConstant {
    override val psi: PsiEnumConstant

    override fun asLogString() = log("name = $name")

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