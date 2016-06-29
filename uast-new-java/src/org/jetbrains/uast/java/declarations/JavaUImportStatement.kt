package org.jetbrains.uast.java

import com.intellij.psi.PsiImportStatementBase
import org.jetbrains.uast.UImportStatement

class JavaUImportStatement(val psi: PsiImportStatementBase) : UImportStatement {
    override val onDemand: Boolean
        get() = psi.isOnDemand

    override fun resolve() = psi.resolve()
}