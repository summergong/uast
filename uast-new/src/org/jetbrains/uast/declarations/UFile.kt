package org.jetbrains.uast

import com.intellij.psi.PsiJavaFile
import org.jetbrains.uast.internal.acceptList
import org.jetbrains.uast.visitor.UastVisitor

interface UFile : UElement {
    val packageName: String
    val imports: List<UImportStatement>
    val classes: List<UClass>

    val languagePlugin: UastLanguagePlugin

    override fun logString() = "UFile"

    override val parent: UElement?
        get() = null

    override fun accept(visitor: UastVisitor) {
        visitor.visitFile(this)
        classes.acceptList(visitor)
        visitor.afterVisitFile(this)
    }
}

