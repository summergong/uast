package org.jetbrains.uast

import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UastConverter
import org.jetbrains.uast.UastLanguagePlugin

class SimpleUMethod(
        override val psi: PsiMethod,
        override val languagePlugin: UastLanguagePlugin,
        override val parent: UElement?
) : UMethod, PsiMethod by psi