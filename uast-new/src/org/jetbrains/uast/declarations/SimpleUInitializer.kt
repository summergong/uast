package org.jetbrains.uast

import com.intellij.psi.PsiClassInitializer
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UastLanguagePlugin

class SimpleUInitializer(
        override val psi: PsiClassInitializer, 
        override val languagePlugin: UastLanguagePlugin, 
        override val parent: UElement?
) : UInitializer, PsiClassInitializer by psi