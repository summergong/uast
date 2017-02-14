package org.jetbrains.uast.java

import com.intellij.psi.PsiPolyadicExpression
import org.jetbrains.uast.*


class JavaUPolyadicExpression(
        override val psi: PsiPolyadicExpression,
        override val uastParent: UElement?
) : JavaAbstractUExpression(), UPolyadicExpression {
    override val operands: List<UExpression> by lz {
        psi.operands.map { JavaConverter.convertOrEmpty(it, this) }
    }

    override val operator: UastBinaryOperator by lz { psi.operationTokenType.getOperatorType() }
}