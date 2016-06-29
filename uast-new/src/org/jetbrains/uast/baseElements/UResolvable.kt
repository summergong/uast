package org.jetbrains.uast

import com.intellij.psi.PsiElement

interface UResolvable {
    fun resolve(): PsiElement?
}