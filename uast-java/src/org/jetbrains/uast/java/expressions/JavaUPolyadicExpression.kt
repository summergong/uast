package org.jetbrains.uast.java

import com.intellij.psi.PsiPolyadicExpression
import org.jetbrains.uast.*
import org.jetbrains.uast.psi.PsiElementBacked


class JavaUPolyadicExpression(
        override val psi: PsiPolyadicExpression,
        override val containingElement: UElement?
) : JavaAbstractUExpression(), UPolyadicExpression, PsiElementBacked {
    override val operands: List<UExpression> by lz {
        psi.operands.map { JavaConverter.convertOrEmpty(it, this) }
    }

    override val operator: UastBinaryOperator by lz { psi.operationTokenType.getOperatorType() }
}