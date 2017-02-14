package org.jetbrains.uast.java.expressions

import com.intellij.psi.PsiNameValuePair
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UNamedExpression
import org.jetbrains.uast.UastEmptyExpression
import org.jetbrains.uast.java.JavaAbstractUExpression
import org.jetbrains.uast.java.JavaConverter
import org.jetbrains.uast.java.lz

class JavaUNamedExpression(
        override val psi: PsiNameValuePair,
        override val uastParent: UElement?
) : JavaAbstractUExpression(), UNamedExpression {
    override fun evaluate(): Any? = expression.evaluate()

    override val name: String?
        get() = psi.name

    override val expression: UExpression by lz {
        psi.value?.let { value -> JavaConverter.convertPsiElement(value, { this }) } as? UExpression ?: UastEmptyExpression }
}
