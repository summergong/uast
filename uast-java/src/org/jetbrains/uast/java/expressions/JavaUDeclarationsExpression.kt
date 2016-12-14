package org.jetbrains.uast.java

import org.jetbrains.uast.UAnnotation
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.UDeclarationsExpression

class JavaUDeclarationsExpression(
        override val containingElement: UElement?
) : UDeclarationsExpression {
    override lateinit var declarations: List<UVariable>
        internal set

    constructor(parent: UElement?, variables: List<UVariable>) : this(parent) {
        this.declarations = variables
    }

    override val annotations: List<UAnnotation>
        get() = emptyList()
}