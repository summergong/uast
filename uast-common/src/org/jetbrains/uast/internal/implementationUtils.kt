package org.jetbrains.uast.internal

import com.intellij.psi.PsiElement
import org.jetbrains.uast.LINE_SEPARATOR
import org.jetbrains.uast.UElement
import org.jetbrains.uast.asLogString
import org.jetbrains.uast.visitor.UastVisitor
import org.jetbrains.uast.withMargin

/**
 * Builds the log message for the [UElement.asLogString] function.
 *
 * @param firstLine the message line (the interface name, some optional information).
 * @param nested nested UElements. Could be `List<UElement>`, [UElement] or `null`.
 * @throws IllegalStateException if the [nested] argument is invalid.
 * @return the rendered log string.
 */
fun UElement.log(firstLine: String, vararg nested: Any?, preferPsi: Boolean = false): String {
    return (if (firstLine.isBlank()) "" else firstLine + LINE_SEPARATOR) + nested.map {
        when (it) {
            null -> "<no element>".withMargin
            is List<*> -> {
                val first = it.firstOrNull()
                @Suppress("UNCHECKED_CAST")
                when {
                    first is UElement && !preferPsi -> (it as List<UElement>).asLogString()
                    first is PsiElement -> (it as List<PsiElement>).joinToString(LINE_SEPARATOR) { it.text }
                    first is UElement -> (it as List<UElement>).asLogString()
                    first == null -> ""
                    else -> error("Invalid element type: $first")
                }
            }
            is UElement -> it.asLogString().withMargin
            else -> error("Invalid element type: $it")
        }
    }.filter(String::isNotEmpty).joinToString(separator = LINE_SEPARATOR)
}

fun List<UElement>.acceptList(visitor: UastVisitor) {
    for (element in this) {
        element.accept(visitor)
    }
}