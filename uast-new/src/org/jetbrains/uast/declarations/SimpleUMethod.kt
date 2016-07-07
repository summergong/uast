package org.jetbrains.uast

import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastConverter
import org.jetbrains.uast.UastLanguagePlugin

class SimpleUMethod(
        psi: PsiMethod,
        override val languagePlugin: UastLanguagePlugin,
        override val containingElement: UElement?
) : UMethod, PsiMethod by psi {
    override val psi = unwrap(psi)
    
    override val uastBody by lz { languagePlugin.getMethodBody(this) ?: UastEmptyExpression }
    override val uastAnnotations by lz { psi.annotations.map { SimpleUAnnotation(it, languagePlugin, this) } }
    
    override val uastParameters by lz {
        psi.parameterList.parameters.map { SimpleUParameter(it, languagePlugin, this) } 
    }
    
    override fun equals(other: Any?) = psi.equals(other)
    override fun hashCode() = psi.hashCode()

    private companion object {
        tailrec fun unwrap(psi: PsiMethod): PsiMethod = if (psi is UMethod) unwrap(psi.psi) else psi
    }
}