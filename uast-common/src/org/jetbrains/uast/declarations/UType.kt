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



/**
 * Represents a type reference.
 */
interface UTypeReference : UDeclaration, UResolvable {
    override fun renderString() = ""
    override fun logString() = log("UTypeReference")

    /**
     * Returns the [UClass] declaration for this type reference.
     *
     * @param context the Uast context
     * @return the [UClass] declaration element, or null if the class was not resolved.
     */
    override fun resolve(context: UastContext): UClass?

    override fun resolveOrEmpty(context: UastContext) = resolve(context) ?: UClassNotResolved
}

object UastErrorType : UType, NoAnnotations {
    override val isInt = false
    override val isLong = false
    override val isShort = false
    override val isFloat = false
    override val isDouble = false
    override val isChar = false
    override val isByte = false
    override val isString = false
    override val isCharSequence = false
    override val isObject = false
    override val isVoid = false
    override val isPrimitive = false
    override val parent = null
    override val arguments = emptyList<UTypeProjection>()
    override val name = ERROR_NAME
    override val fqName = null
    override val isBoolean = false
    override fun resolveToClass(context: UastContext) = null
    override fun resolve() = UResolvedErrorType
}