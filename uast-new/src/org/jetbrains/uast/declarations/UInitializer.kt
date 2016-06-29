package org.jetbrains.uast

import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiModifierListOwner
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UElement
import org.jetbrains.uast.visitor.UastVisitor

interface UInitializer : UDeclaration, PsiClassInitializer {
    override val psi: PsiClassInitializer

    override fun accept(visitor: UastVisitor) {
        visitor.visitInitializer(this)
        visitor.afterVisitInitializer(this)
    }

    override fun logString() = "UMethod (name = ${psi.name}"
}