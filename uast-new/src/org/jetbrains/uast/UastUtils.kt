@file:JvmMultifileClass
@file:JvmName("UastUtils")
package org.jetbrains.uast

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

@JvmOverloads
fun <T : UElement> UElement.getParentOfType(parentClass: Class<out UElement>, strict: Boolean = true): T? {
    var element = (if (strict) parent else this) ?: return null
    while (true) {
        if (parentClass.isInstance(element)) {
            @Suppress("UNCHECKED_CAST")
            return element as T
        }
        element = element.parent ?: return null
    }
}

fun UElement.getContainingUMethod() = getParentOfType<UMethod>(UMethod::class.java)
fun UElement.getContainingUVariable() = getParentOfType<UVariable>(UVariable::class.java)

fun UElement.getContainingMethod() = getContainingUMethod()?.psi
fun UElement.getContainingVariable() = getContainingUVariable()?.psi

fun PsiElement?.getContainingClass() = this?.let { PsiTreeUtil.getParentOfType(it, PsiClass::class.java) }