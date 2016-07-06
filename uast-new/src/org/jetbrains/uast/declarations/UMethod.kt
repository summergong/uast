package org.jetbrains.uast

import com.intellij.psi.*
import org.jetbrains.uast.visitor.UastVisitor

interface UMethod : UDeclaration, PsiMethod {
    override val psi: PsiMethod
    val uastBody: UExpression
    
    val uastParameters: List<UParameter>

    @Deprecated("Use uastBody instead.", ReplaceWith("uastBody"))
    override fun getBody() = psi.body

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitMethod(this)) return
        uastBody.accept(visitor)
        visitor.afterVisitMethod(this)
    }

    override fun logString() = "UMethod (name = $name"
}