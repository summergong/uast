package org.jetbrains.uast.internal

import org.jetbrains.uast.UElement
import org.jetbrains.uast.visitor.UastVisitor

fun List<UElement>.acceptList(visitor: UastVisitor) {
    for (element in this) {
        element.accept(visitor)
    }
}

@Suppress("unused")
inline fun <reified T : UElement> T.log(text: String = ""): String {
    val className = T::class.java.simpleName
    return if (text.isEmpty()) className else "$className ($text)"
}