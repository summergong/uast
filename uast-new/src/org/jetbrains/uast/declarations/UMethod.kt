package org.jetbrains.uast

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassInitializer
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import org.jetbrains.uast.visitor.UastVisitor

interface UMethod : UDeclaration, PsiMethod {
    override val psi: PsiMethod
    
    val uastBody: UExpression
        get() = languagePlugin.getMethodBody(this)!!

    override fun accept(visitor: UastVisitor) {
        visitor.visitMethod(this)
        uastBody.accept(visitor)
        visitor.afterVisitMethod(this)
    }

    override fun logString() = "UMethod (name = $name"
}