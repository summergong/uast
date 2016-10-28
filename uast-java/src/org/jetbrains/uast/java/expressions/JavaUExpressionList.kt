package org.jetbrains.uast.java.expressions

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UExpressionList
import org.jetbrains.uast.UastSpecialExpressionKind
import org.jetbrains.uast.java.JavaAbstractUExpression
import org.jetbrains.uast.psi.PsiElementBacked

open class JavaUExpressionList(
        override val psi: PsiElement?,
        override val kind: UastSpecialExpressionKind, // original element
        override val containingElement: UElement?
) : JavaAbstractUExpression(), UExpressionList, PsiElementBacked {
    override lateinit var expressions: List<UExpression>
        internal set
}