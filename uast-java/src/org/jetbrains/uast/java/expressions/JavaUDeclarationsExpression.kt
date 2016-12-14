package org.jetbrains.uast.java

import org.jetbrains.uast.*

class JavaUDeclarationsExpression(
        override val containingElement: UElement?
) : UDeclarationsExpression {
    override lateinit var declarations: List<UDeclaration>
        internal set

    constructor(parent: UElement?, declarations: List<UDeclaration>) : this(parent) {
        this.declarations = declarations
    }

    override val annotations: List<UAnnotation>
        get() = emptyList()
}