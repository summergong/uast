package org.jetbrains.uast

import com.intellij.psi.PsiElement
import org.jetbrains.uast.internal.log

class UComment(override val psi: PsiElement, override val uastParent: UElement) : UElement {
    val text: String
        get() = asSourceString()

    override fun asLogString() = log()

    override fun asRenderString(): String = asSourceString()
    override fun asSourceString(): String = psi.text
}