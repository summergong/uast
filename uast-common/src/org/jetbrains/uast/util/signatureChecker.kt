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

@file:JvmName("UastSignatureChecker")
package org.jetbrains.uast.util

import org.jetbrains.uast.UFunction
import org.jetbrains.uast.UResolvedArrayType
import org.jetbrains.uast.UResolvedType
import org.jetbrains.uast.UType

class UTypeConstraint private constructor(
        val fqName: String?,
        val allowPrimitive: Boolean,
        val allowBoxed: Boolean,
        val check: ((UType) -> Boolean)?
) {
    interface Check {
        operator fun invoke(type: UType): Boolean
    }

    interface ResolvedCheck {
        operator fun invoke(type: UResolvedType): Boolean
    }

    companion object {
        @JvmField
        val ANYTHING = UTypeConstraint(null, true, true, null)

        @JvmField
        val INT = UTypeConstraint(null, true, true) { it.isInt }
        @JvmField
        val SHORT = UTypeConstraint(null, true, true, { it.isShort })
        @JvmField
        val LONG = UTypeConstraint(null, true, true, { it.isLong })
        @JvmField
        val FLOAT = UTypeConstraint(null, true, true, { it.isFloat })
        @JvmField
        val DOUBLE = UTypeConstraint(null, true, true, { it.isDouble })
        @JvmField
        val CHAR = UTypeConstraint(null, true, true, { it.isChar })
        @JvmField
        val BOOLEAN = UTypeConstraint(null, true, true, { it.isBoolean })
        @JvmField
        val BYTE = UTypeConstraint(null, true, true, { it.isByte })
        @JvmField
        val VOID = UTypeConstraint(null, true, true, { it.isVoid })

        @JvmField
        val PRIMITIVE_INT = UTypeConstraint(null, true, false, { it.isInt })
        @JvmField
        val PRIMITIVE_SHORT = UTypeConstraint(null, true, false, { it.isShort })
        @JvmField
        val PRIMITIVE_LONG = UTypeConstraint(null, true, false, { it.isLong })
        @JvmField
        val PRIMITIVE_FLOAT = UTypeConstraint(null, true, false, { it.isFloat })
        @JvmField
        val PRIMITIVE_DOUBLE = UTypeConstraint(null, true, false, { it.isDouble })
        @JvmField
        val PRIMITIVE_CHAR = UTypeConstraint(null, true, false, { it.isChar })
        @JvmField
        val PRIMITIVE_BOOLEAN = UTypeConstraint(null, true, false, { it.isBoolean })
        @JvmField
        val PRIMITIVE_BYTE = UTypeConstraint(null, true, false, { it.isByte })
        @JvmField
        val PRIMITIVE_VOID = UTypeConstraint(null, true, false, { it.isVoid })

        @JvmField
        val BOXED_INT = UTypeConstraint(null, false, true, { it.isInt })
        @JvmField
        val BOXED_SHORT = UTypeConstraint(null, false, true, { it.isShort })
        @JvmField
        val BOXED_LONG = UTypeConstraint(null, false, true, { it.isLong })
        @JvmField
        val BOXED_FLOAT = UTypeConstraint(null, false, true, { it.isFloat })
        @JvmField
        val BOXED_DOUBLE = UTypeConstraint(null, false, true, { it.isDouble })
        @JvmField
        val BOXED_CHAR = UTypeConstraint(null, false, true, { it.isChar })
        @JvmField
        val BOXED_BOOLEAN = UTypeConstraint(null, false, true, { it.isBoolean })
        @JvmField
        val BOXED_BYTE = UTypeConstraint(null, false, true, { it.isByte })
        @JvmField
        val BOXED_VOID = UTypeConstraint(null, false, true, { it.isVoid })

        @JvmField
        val STRING = UTypeConstraint(null, true, true, { it.isString })
        @JvmField
        val CHAR_SEQUENCE = UTypeConstraint(null, true, true, { it.isCharSequence })
        @JvmField
        val ARRAY = UTypeConstraint(null, true, true, { it.resolve() is UResolvedArrayType })
        @JvmField
        val OBJECT = UTypeConstraint(null, true, true, { it.isObject })
        
        @JvmStatic
        fun make(fqName: String) = UTypeConstraint(fqName, true, true, null)

        @JvmStatic
        fun make(check: Check) = UTypeConstraint(null, true, true, { check(it) })

        @JvmStatic
        fun make(check: ResolvedCheck) = UTypeConstraint(null, true, true, { check(it.resolve()) })

        @JvmStatic
        fun makeArrayOf(check: Check) = UTypeConstraint(null, true, true, {
            val resolvedType = it.resolve()
            resolvedType is UResolvedArrayType && check(resolvedType.elementType)
        })
    }
}

fun UType.matchesConstraint(constraint: UTypeConstraint) : Boolean {
    val fqName = constraint.fqName
    if (fqName != null && !matchesFqName(fqName)) return false

    val allowPrimitive = constraint.allowPrimitive
    val allowBoxed = constraint.allowBoxed
    if (allowPrimitive && !allowBoxed && !isPrimitive) return false
    if (!allowPrimitive && allowBoxed && isPrimitive) return false

    val check = constraint.check
    if (check != null && !check(this)) return false

    return true
}

fun UFunction.matchesSignature(vararg fqNames: String): Boolean {
    if (valueParameterCount != fqNames.size) {
        return false
    }

    val params = valueParameters
    for (index in (0..fqNames.size - 1)) {
        val type = params[index].type
        val fqName = fqNames[index]
        if (!type.matchesFqName(fqName)) return false
    }

    return true
}

fun UFunction.matchesSignature(vararg constraints: UTypeConstraint): Boolean {
    if (valueParameterCount != constraints.size) {
        return false
    }

    val params = valueParameters
    for (index in (0..constraints.size - 1)) {
        val type = params[index].type
        val constraint = constraints[index]
        if (!type.matchesConstraint(constraint)) return false
    }
    
    return true
}