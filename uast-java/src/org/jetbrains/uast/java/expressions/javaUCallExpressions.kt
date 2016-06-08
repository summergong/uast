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
package org.jetbrains.uast.java

import com.intellij.psi.PsiArrayInitializerExpression
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.uast.*
import org.jetbrains.uast.psi.PsiElementBacked


class JavaUCallExpression(
        override val psi: PsiMethodCallExpression,
        override val parent: UElement?
) : JavaAbstractUExpression(), UCallExpression, PsiElementBacked {
    override val kind: UastCallKind
        get() = UastCallKind.FUNCTION_CALL

    override val functionReference by lz { JavaConverter.convertExpression(psi.methodExpression, this) as? USimpleReferenceExpression }

    override val classReference: USimpleReferenceExpression?
        get() = null

    override val valueArgumentCount by lz { psi.argumentList.expressions.size }
    override val valueArguments by lz { psi.argumentList.expressions.map { JavaConverter.convertExpression(it, this) } }

    override val typeArgumentCount by lz { psi.typeArguments.size }
    override val typeArguments by lz { psi.typeArguments.map { JavaConverter.convertType(it) } }

    override val functionName: String
        get() = psi.methodExpression.referenceName ?: "<error name>"

    override val functionNameElement by lz { JavaDumbUElement(psi.methodExpression.referenceNameElement, this) }

    override fun resolve(context: UastContext) = psi.resolveMethod()?.let { context.convert(it) as? UFunction }

    override fun resolveType(context: UastContext) = null
}

class JavaConstructorUCallExpression(
        override val psi: PsiNewExpression,
        override val parent: UElement?
) : JavaAbstractUExpression(), UCallExpression, PsiElementBacked {
    override val kind by lz {
        when {
            psi.arrayInitializer != null -> UastCallKind.NEW_ARRAY_WITH_INITIALIZER
            psi.arrayDimensions.isNotEmpty() -> UastCallKind.NEW_ARRAY_WITH_DIMENSIONS
            else -> UastCallKind.CONSTRUCTOR_CALL
        }
    }

    override val functionReference: USimpleReferenceExpression?
        get() = null

    override val classReference by lz {
        psi.classReference?.let { ref ->
            JavaClassUSimpleReferenceExpression(ref.element?.text.orAnonymous(), ref, ref.element, this)
        }
    }

    override val valueArgumentCount: Int
        get() {
            val initializer = psi.arrayInitializer
            return if (initializer != null) {
                initializer.initializers.size
            } else if (psi.arrayDimensions.isNotEmpty()) {
                psi.arrayDimensions.size
            } else {
                psi.argumentList?.expressions?.size ?: 0
            }
        }

    override val valueArguments by lz {
        val initializer = psi.arrayInitializer
        if (initializer != null) {
            initializer.initializers.map { JavaConverter.convertExpression(it, this) }
        }
        else if (psi.arrayDimensions.isNotEmpty()) {
            psi.arrayDimensions.map { JavaConverter.convertExpression(it, this) }
        }
        else {
            psi.argumentList?.expressions?.map { JavaConverter.convertExpression(it, this) } ?: emptyList()
        }
    }

    override val typeArgumentCount by lz { psi.classReference?.typeParameters?.size ?: 0 }
    override val typeArguments by lz {
        psi.classReference?.typeParameters?.map { JavaConverter.convertType(it) } ?: listOf() 
    }

    override val functionName: String?
        get() {
            val initializer = psi.arrayInitializer
            return if (initializer != null)
                "<newArrayWithInitializer>"
            else if (psi.arrayDimensions.isNotEmpty())
                "<newArrayWithDimensions>"
            else null
        }

    override val functionNameElement by lz { JavaDumbUElement(psi, this) }

    override fun resolve(context: UastContext) = psi.resolveConstructor()?.let { context.convert(it) } as? UFunction

    private fun resolveUsingExpressionType(context: UastContext): UType? {
        val type = psi.type ?: return null
        return context.convert(type) as? UType
    }

    override fun resolveType(context: UastContext): UType? {
        val constructorClass = psi.resolveConstructor()?.containingClass ?: return resolveUsingExpressionType(context)
        val type = PsiTypesUtil.getClassType(constructorClass)
        return context.convert(type) as? UType
    }
}

class JavaArrayInitializerUCallExpression(
        override val psi: PsiArrayInitializerExpression,
        override val parent: UElement?
) : JavaAbstractUExpression(), UCallExpression, PsiElementBacked {
    override val functionReference: USimpleReferenceExpression?
        get() = null

    override val classReference: USimpleReferenceExpression?
        get() = null

    override val functionName: String
        get() = "<array>"

    override val functionNameElement: UElement?
        get() = null

    override val valueArgumentCount by lz { psi.initializers.size }
    override val valueArguments by lz { psi.initializers.map { JavaConverter.convertExpression(it, this) } }

    override val typeArgumentCount: Int
        get() = 0

    override val typeArguments: List<UType>
        get() = emptyList()

    override val kind: UastCallKind
        get() = UastCallKind.NESTED_ARRAY_INITIALIZER

    override fun resolve(context: UastContext) = null

    override fun resolveType(context: UastContext): UType? {
        val type = psi.type?.unwrapArrayType() ?: return null
        return context.convert(type) as? UType
    }
}

class JavaAnnotationArrayInitializerUCallExpression(
        override val psi: PsiArrayInitializerMemberValue,
        override val parent: UElement?
) : JavaAbstractUExpression(), UCallExpression, PsiElementBacked {
    override val kind: UastCallKind
        get() = UastCallKind.NESTED_ARRAY_INITIALIZER

    override val functionReference: USimpleReferenceExpression?
        get() = null

    override val classReference: USimpleReferenceExpression?
        get() = null

    override val functionName: String
        get() = "<annotationArray>"

    override val functionNameElement: UElement?
        get() = null

    override val valueArgumentCount by lz { psi.initializers.size }
    override val valueArguments by lz {
        psi.initializers.map {
            JavaConverter.convert(it, this) as? UExpression ?: UnknownJavaExpression(it, this)
        }
    }

    override val typeArgumentCount: Int
        get() = 0

    override val typeArguments: List<UType>
        get() = emptyList()

    override fun resolve(context: UastContext) = null

    override fun resolveType(context: UastContext) = null
}