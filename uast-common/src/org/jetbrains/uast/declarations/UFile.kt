package org.jetbrains.uast

import com.intellij.psi.PsiFile
import org.jetbrains.uast.internal.acceptList
import org.jetbrains.uast.psi.PsiElementBacked
import org.jetbrains.uast.visitor.UastTypedVisitor
import org.jetbrains.uast.visitor.UastVisitor
import org.jetbrains.uast.internal.log

/**
 * Represents a Uast file.
 */
interface UFile : UElement, UAnnotated, PsiElementBacked {
    /**
     * Returns the original [PsiFile].
     */
    override val psi: PsiFile

    /**
     * Returns the Java package name of this file.
     * Returns an empty [String] for the default package. 
     */
    val packageName: String

    /**
     * Returns the import statements for this file.
     */
    val imports: List<UImportStatement>

    /**
     * Returns the list of top-level classes declared in this file.
     */
    val classes: List<UClass>

    /**
     * Returns the plugin for a language used in this file.
     */
    val languagePlugin: UastLanguagePlugin

    /**
     * Returns all comments in file.
     */
    val allCommentsInFile: List<UComment>

    override fun asLogString() = log("UFile", annotations, imports, classes)

    override fun asRenderString() = log("UFile", annotations, imports, classes, preferPsi = true)

    /**
     * [UFile] is a top-level element of the Uast hierarchy, thus the [containingElement] always returns null for it.
     */
    override val containingElement: UElement?
        get() = null

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitFile(this)) return
        annotations.acceptList(visitor)
        imports.acceptList(visitor)
        classes.acceptList(visitor)
        visitor.afterVisitFile(this)
    }

    override fun <D, R> accept(visitor: UastTypedVisitor<D, R>, data: D) =
            visitor.visitFile(this, data)
}

