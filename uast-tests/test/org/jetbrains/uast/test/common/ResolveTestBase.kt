package org.jetbrains.uast.test.common

import com.intellij.psi.PsiNamedElement
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UFile
import org.jetbrains.uast.UResolvable
import org.jetbrains.uast.psi.PsiElementBacked
import org.jetbrains.uast.visitor.UastVisitor
import org.junit.Assert.assertEquals

interface ResolveTestBase {
    fun check(testName: String, file: UFile) {
        val refComment = file.allCommentsInFile.find { it.text.startsWith("// REF:") } ?: throw IllegalArgumentException("No // REF tag in file")
        val resultComment = file.allCommentsInFile.find { it.text.startsWith("// RESULT:") } ?: throw IllegalArgumentException("No // RESULT tag in file")

        val refText = refComment.text.substringAfter("REF:")
        val parent = refComment.containingElement
        val matchingElements = mutableListOf<UResolvable>()
        parent.accept(object : UastVisitor {
            override fun visitElement(node: UElement): Boolean {
                if (node is PsiElementBacked && node.psi!!.text == refText && node is UResolvable) {
                    matchingElements.add(node)
                }
                return false
            }
        })

        if (matchingElements.isEmpty()) {
            throw IllegalArgumentException("Reference '$refText' not found")
        }
        if (matchingElements.size != 1) {
            throw IllegalArgumentException("Reference '$refText' is ambiguous")
        }
        val resolveResult = matchingElements.single().resolve() ?: throw IllegalArgumentException("Unresolved reference")
        val resultText = resolveResult.javaClass.simpleName + (if (resolveResult is PsiNamedElement) ":${resolveResult.name}" else "")
        assertEquals(resultComment.text.substringAfter("RESULT:"), resultText)
    }
}
