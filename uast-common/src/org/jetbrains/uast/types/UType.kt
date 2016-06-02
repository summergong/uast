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
 * Represents the type.
 * The abstraction is quite simple. Intersection types, union types, platform types are yet to be supported.
 */
interface UType : UElement, UNamed, UFqNamed, UAnnotated {
    /**
     * Returns the simple (non-qualified) type name.
     * The simple type name is only for the debug purposes. Do not check against it in the production code.
     */
    override val name: String

    /**
     * Returns true if the type is either a boxed or an unboxed [Integer], false otherwise.
     */
    val isInt: Boolean

    /**
     * Returns true if the type is either a boxed or an unboxed [Short], false otherwise.
     */
    val isShort: Boolean

    /**
     * Returns true if the type is either a boxed or an unboxed [Long], false otherwise.
     */
    val isLong: Boolean

    /**
     * Returns true if the type is either a boxed or an unboxed [Float], false otherwise.
     */
    val isFloat: Boolean

    /**
     * Returns true if the type is either a boxed or an unboxed [Double], false otherwise.
     */
    val isDouble: Boolean

    /**
     * Returns true if the type is either a boxed or an unboxed [Character], false otherwise.
     */
    val isChar: Boolean

    /**
     * Returns true if the type is either a boxed or an unboxed [Boolean], false otherwise.
     */
    val isBoolean: Boolean

    /**
     * Returns true if the type is either a boxed or an unboxed [Byte], false otherwise.
     */
    val isByte: Boolean

    /**
     * Returns true if the type is [lava.lang.String], false otherwise.
     * Some languages might provide own [lava.lang.String] wrappers,
     *  so calling [isString] is more reliable than checking [fqName] manually.
     */
    val isString: Boolean

    /**
     * Returns true if the type is [java.lang.CharSequence], false otherwise.
     * Some languages might provide own [lava.lang.CharSequence] wrappers,
     *  so calling [isCharSequence] is more reliable than checking [fqName] manually.
     */
    val isCharSequence: Boolean

    /**
     * Returns true if the type is [java.lang.Object], false otherwise.
     * Some languages might provide own [lava.lang.Object] wrappers,
     *  so calling [isObject] is more reliable than checking [fqName] manually.
     */
    val isObject: Boolean

    /**
     * Returns true if the type is either boxed or an unboxed [Void], false otherwise.
     */
    val isVoid: Boolean

    /**
     * Returns true if the type is a primitive type.
     * Calculation of this value could potentially be expensive, because in some languages (e.g. Scala, Kotlin)
     *  there are no primitive types, and the actual bytecode type is calculated during the compile time.
     * If you are uncertain if the generated type will be primitive or not, return `false`.
     */
    val isPrimitive: Boolean

    /**
     * Returns true if the type is a built-in type.
     * Complete built-in type list: int, short, long, float, double, char, boolean, byte, void (or its boxed wrappers), and String.
     */
    val isBuiltinType: Boolean
        get() = isInt || isShort || isLong || isFloat || isDouble || isChar || isBoolean || isByte || isString || isVoid

    /**
     * Returns the list of type parameters of this type.
     */
    val arguments: List<UTypeProjection>

    /**
     * [parent] should always return 'null' for [UType].
     */
    override val parent: UElement?
        get() = null

    fun resolve(): UResolvedType

    /**
     * Returns the [UClass] declaration element for this type.
     *
     * @param context the Uast context
     * @return the [UClass] declaration element, or null if the class was not resolved.
     */
    fun resolveToClass(context: UastContext): UClass?

    fun resolveToClassOrEmpty(context: UastContext) = resolveToClass(context) ?: UClassNotResolved

    override fun logString() = "UType ($name)"
    override fun renderString() = name

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitType(this)) return
        annotations.acceptList(visitor)
        visitor.afterVisitType(this)
    }
}