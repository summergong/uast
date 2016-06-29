package org.jetbrains.uast

import com.intellij.psi.PsiField
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiVariable
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.UastLanguagePlugin

class SimpleUVariable(
        override val psi: PsiVariable, 
        override val languagePlugin: UastLanguagePlugin, 
        override val parent: UElement?
) : UVariable, PsiVariable by psi

class SimpleUParameter(
        override val psi: PsiParameter,
        override val languagePlugin: UastLanguagePlugin,
        override val parent: UElement?
) : UParameter, PsiParameter by psi

class SimpleUField(
        override val psi: PsiField,
        override val languagePlugin: UastLanguagePlugin,
        override val parent: UElement?
) : UVariable, PsiField by psi

class SimpleULocalVariable(
        override val psi: PsiLocalVariable,
        override val languagePlugin: UastLanguagePlugin,
        override val parent: UElement?
) : UVariable, PsiLocalVariable by psi
