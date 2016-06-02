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
package org.jetbrains.uast

import org.jetbrains.uast.visitor.UastVisitor

/**
 * Represents a declaration.
 */
interface UDeclaration : UElement, UNamed {
    /**
     * Returns an element for the name node, or null if the node does not exist in the underlying AST (Psi).
     */
    val nameElement: UElement?

    /**
     * Get the list of all overridden declarations for this declaration.
     *
     * @param context the Uast context
     * @return returns the list of all overridden declaration. Order is not guaranteed.
     */
    fun getOverriddenDeclarations(context: UastContext): List<UDeclaration>

    /**
     * Checks if the declaration's containing class qualified name is [containingClassFqName].
     *
     * @param containingClassFqName the required containing class qualified name.
     * @return true if the qualified name of the containing class is [containingClassFqName],
     *         false otherwise.
     */
    fun matchesContaining(containingClassFqName: String): Boolean {
        return matchesContaining(containingClassFqName, false)
    }

    /**
     * Checks if the declaration's containing class qualified name is [containingClassFqName].
     *
     * @param containingClassFqName the required containing class qualified name.
     * @param allowInherit if true, the containing class could also a subtype of [containingClassFqName].
     * @return true if the qualified name of the containing class is [containingClassFqName],
     *         false otherwise.
     */
    fun matchesContaining(containingClassFqName: String, allowInherit: Boolean): Boolean {
        val containingClass = this.getContainingClass() ?: return false
        val matchesExactly = containingClass.matchesFqName(containingClassFqName)
        if (!allowInherit || matchesExactly) return matchesExactly
        return containingClass.isSubclassOf(containingClassFqName, true)
    }

    /**
     * Checks if the declaration name is [name], and the declaration's containing class qualified name is [containingClassFqName].
     *
     * @param containingClassFqName the required containing class qualified name.
     * @param name the declaration name to check against.
     * @return true if the declaration name is [name],
     *              and the qualified name of the containing class is [containingClassFqName],
     *         false otherwise.
     */
    fun matchesNameWithContaining(containingClassFqName: String, name: String): Boolean {
        if (!matchesName(name)) return false
        return matchesContaining(containingClassFqName)
    }

    override fun accept(visitor: UastVisitor) {
        visitor.visitElement(this)
        visitor.afterVisitElement(this)
    }
}

object UDeclarationNotResolved : UDeclaration {
    override val name = ERROR_NAME
    override val nameElement = null
    override val parent = null

    override fun logString() = "[!] $name"
    override fun renderString() = name

    override fun getOverriddenDeclarations(context: UastContext) = emptyList<UDeclaration>()
}