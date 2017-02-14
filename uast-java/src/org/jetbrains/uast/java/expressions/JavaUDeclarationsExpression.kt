package org.jetbrains.uast.java

import com.intellij.psi.PsiElement
import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UDeclarationsExpression
import org.jetbrains.uast.UElement

class JavaUDeclarationsExpression(
        override val uastParent: UElement?
) : UDeclarationsExpression {
    override lateinit var declarations: List<UDeclaration>
        internal set

    constructor(parent: UElement?, declarations: List<UDeclaration>) : this(parent) {
        this.declarations = declarations
    }

    override val annotations: List<UAnnotation>
        get() = emptyList()

    override val psi: PsiElement?
        get() = null
}