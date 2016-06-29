package org.jetbrains.uast

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiVariable
import org.jetbrains.uast.visitor.UastVisitor

interface UVariable : UDeclaration, PsiVariable {
    override val psi: PsiVariable

    val uastInitializer: UExpression?
        get() = languagePlugin.getInitializerBody(psi)

    override fun accept(visitor: UastVisitor) {
        visitor.visitVariable(this)
        uastInitializer?.accept(visitor)
        visitor.afterVisitVariable(this)
    }

    override fun logString() = "UVariable (name = $name)"
}

interface UParameter : UVariable {
    override val psi: PsiParameter
}