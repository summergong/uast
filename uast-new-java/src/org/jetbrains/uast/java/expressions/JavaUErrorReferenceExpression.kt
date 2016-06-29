package org.jetbrains.uast.java

import org.jetbrains.uast.UElement
import org.jetbrains.uast.expressions.UReferenceExpression

class JavaUErrorReferenceExpression(override val parent: UElement?) : UReferenceExpression {
    override fun resolve() = null
}