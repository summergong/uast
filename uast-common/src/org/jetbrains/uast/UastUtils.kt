/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:JvmMultifileClass
@file:JvmName("UastUtils")
package org.jetbrains.uast

import org.jetbrains.uast.visitor.UastVisitor

/**
 * Returns the containing class of an element.
 *
 * @return the containing [UClass] element,
 *         or null if the receiver is null, or it is a top-level declaration.
 */
tailrec fun UElement?.getContainingClass(): UClass? {
    val parent = this?.parent ?: return null
    if (parent is UClass) return parent
    return parent.getContainingClass()
}

/**
 * Returns the containing file of an element.
 *
 * @return the containing [UFile] element,
 *         or null if the receiver is null, or the element is not inside a [UFile] (it is abnormal).
 */
tailrec fun UElement?.getContainingFile(): UFile? {
    val parent = this?.parent ?: return null
    if (parent is UFile) return parent
    return parent.getContainingFile()
}

fun UElement?.getContainingClassOrEmpty() = getContainingClass() ?: UClassNotResolved

/**
 * Returns the containing function of an element.
 *
 * @return the containing [UFunction] element,
 *         or null if the receiver is null, or the element is not inside a [UFunction].
 */
tailrec fun UElement?.getContainingFunction(): UFunction? {
    val parent = this?.parent ?: return null
    if (parent is UFunction) return parent
    return parent.getContainingFunction()
}

/**
 * Returns the containing declaration of an element.
 *
 * @return the containing [UDeclaration] element,
 *         or null if the receiver is null, or the element is a top-level declaration.
 */
tailrec fun UElement?.getContainingDeclaration(): UDeclaration? {
    val parent = this?.parent ?: return null
    if (parent is UDeclaration) return parent
    return parent.getContainingDeclaration()
}

/**
 * Checks if the element is a top-level declaration.
 *
 * @return true if the element is a top-level declaration, false otherwise.
 */
fun UDeclaration.isTopLevel() = parent is UFile

/**
 * Builds the log message for the [UElement.logString] function.
 *
 * @param firstLine the message line (the interface name, some optional information).
 * @param nested nested UElements. Could be `List<UElement>`, [UElement] or `null`.
 * @throws IllegalStateException if the [nested] argument is invalid.
 * @return the rendered log string.
 */
fun UElement.log(firstLine: String, vararg nested: Any?): String {
    return (if (firstLine.isBlank()) "" else firstLine + LINE_SEPARATOR) + nested.joinToString(LINE_SEPARATOR) {
        when (it) {
            null -> "<no element>".withMargin
            is List<*> ->
                @Suppress("UNCHECKED_CAST")
                (it as List<UElement>).logString()
            is UElement -> it.logString().withMargin
            else -> error("Invalid element type: $it")
        }
    }
}

fun List<UElement>.acceptList(visitor: UastVisitor) {
    for (element in this) {
        element.accept(visitor)
    }
}

@Suppress("UNCHECKED_CAST")
fun UClass.findFunctions(name: String): List<UFunction> {
    return declarations.filter { it is UFunction && it.matchesName(name) } as List<UFunction>
}

fun UClass.findVariable(name: String): UVariable? {
    return declarations.firstOrNull { it is UVariable && it.matchesName(name) } as? UVariable
}

fun UCallExpression.getReceiver(): UExpression? = (this.parent as? UQualifiedExpression)?.receiver

/**
 * Resolves the receiver element if it implements [UResolvable].
 *
 * @return the resolved element, or null if the element was not resolved, or if the receiver element is not an [UResolvable].
 */
fun UElement.resolveIfCan(context: UastContext): UDeclaration? = (this as? UResolvable)?.resolve(context)

/**
 * Get all class declarations (including properties and functions in class supertypes).
 *
 * @param context the Uast context
 * @return the list of declarations for the receiver class
 */
fun UClass.getAllDeclarations(context: UastContext): List<UDeclaration> = mutableListOf<UDeclaration>().apply {
    this += declarations
    for (superClass in getOverriddenDeclarations(context)) {
        this += superClass.declarations
    }
}

/**
 * Get all functions in class (including supertypes).
 *
 * @param context the Uast context
 * @return the list of functions for the receiver class and its supertypes
 */
fun UClass.getAllFunctions(context: UastContext) = getAllDeclarations(context).filterIsInstance<UFunction>()

/**
 * Get the nearest parent of the type [T].
 *
 * @return the nearest parent of type [T], or null if the parent with such type was not found.
 */
inline fun <reified T: UElement> UElement.getParentOfType(strict: Boolean = true): T? = getParentOfType(T::class.java, strict)


fun <T: UElement> UElement.getParentOfType(clazz: Class<T>): T? = getParentOfType(clazz, strict = true)

/**
 * Get the nearest parent of the type [T].
 *
 * @param strict if false, return the received element if it's type is [T], do not check the received element overwise.
 * @return the nearest parent of type [T], or null if the parent with such type was not found.
 */
fun <T: UElement> UElement.getParentOfType(clazz: Class<T>, strict: Boolean): T? {
    tailrec fun findParent(element: UElement?): UElement? {
        return when {
            element == null -> null
            clazz.isInstance(element) -> element
            else -> findParent(element.parent)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return findParent(if (strict) parent else this) as T?
}

fun <T: UElement> UElement.getParentOfType(clazz: Class<T>, strict: Boolean, vararg stopAt: Class<out UElement>): T? {
    tailrec fun findParent(element: UElement?): UElement? {
        return when {
            element == null -> null
            clazz.isInstance(element) -> element
            stopAt.any { it.isInstance(element) } -> null
            else -> findParent(element.parent)
        }
    }

    @Suppress("UNCHECKED_CAST")
    return findParent(if (strict) parent else this) as T?
}

fun <T> UClass.findStaticMemberOfType(name: String, type: Class<out T>): T? {
    for (companion in companions) {
        val member = companion.declarations.firstOrNull {
            it.name == name && type.isInstance(it) && it is UModifierOwner && it.hasModifier(UastModifier.STATIC)
        }
        @Suppress("UNCHECKED_CAST")
        if (member != null) return member as T?
    }

    @Suppress("UNCHECKED_CAST")
    return declarations.firstOrNull {
        it.name == name && it is UModifierOwner
                && it.hasModifier(UastModifier.STATIC) && type.isInstance(it)
    } as T?
}

/**
 * Checks if the received [UElement] is a child of [maybeParent].
 *
 * @return true if the received element is a child of [maybeParent], false otherwise.
 */
tailrec fun UElement.isChild(maybeParent: UElement): Boolean {
    val parent = this.parent ?: return false
    if (parent == maybeParent) return true
    return parent.isChild(maybeParent)
}