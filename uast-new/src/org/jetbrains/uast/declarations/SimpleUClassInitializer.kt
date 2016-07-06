package org.jetbrains.uast

import com.intellij.psi.PsiClassInitializer
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UastLanguagePlugin

class SimpleUClassInitializer(
        override val psi: PsiClassInitializer, 
        override val languagePlugin: UastLanguagePlugin, 
        override val containingElement: UElement?
) : UClassInitializer, PsiClassInitializer by psi {
    override val uastBody by lz { languagePlugin.getInitializerBody(this) ?: UastEmptyExpression }
    
    override fun equals(other: Any?) = psi.equals(other)
    override fun hashCode() = psi.hashCode()
}