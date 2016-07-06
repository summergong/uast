package org.jetbrains.uast

import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastConverter
import org.jetbrains.uast.UastLanguagePlugin

class SimpleUMethod(
        override val psi: PsiMethod,
        override val languagePlugin: UastLanguagePlugin,
        override val containingElement: UElement?
) : UMethod, PsiMethod by psi {
    override val uastBody by lz { languagePlugin.getMethodBody(this) ?: UastEmptyExpression }
    
    override val uastParameters by lz {
        psi.parameterList.parameters.map { SimpleUParameter(it, languagePlugin, this) } 
    }
    
    override fun equals(other: Any?) = psi.equals(other)
    override fun hashCode() = psi.hashCode()
}