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

import com.intellij.psi.*
import com.intellij.psi.util.PsiTypesUtil
import org.jetbrains.uast.*
import org.jetbrains.uast.psi.PsiElementBacked

class JavaUCallExpression(
        override val psi: PsiMethodCallExpression,
        override val parent: UElement?
) : JavaAbstractUExpression(), UCallExpression, PsiElementBacked {
    override val kind: UastCallKind
        get() = UastCallKind.FUNCTION_CALL

    override val functionReference by lz { JavaConverter.convertExpression(psi.methodExpression, this) as? USimpleNameReferenceExpression }

    override val classReference: USimpleNameReferenceExpression?
        get() = null

    override val valueArgumentCount by lz { psi.argumentList.expressions.size }
    override val valueArguments by lz { psi.argumentList.expressions.map { JavaConverter.convertExpression(it, this) } }

    override val typeArgumentCount by lz { psi.typeArguments.size }

    override val typeArguments: List<PsiType>
        get() = psi.typeArguments.toList()
    
    override val name: String?
        get() = psi.methodExpression.referenceName

    override fun resolve() = psi.resolveMethod()

    override val receiver: UExpression?
        get() {
            return if (parent is UQualifiedReferenceExpression && parent.selector == this)
                parent.receiver
            else
                null
        }
    
    override val receiverType: PsiType?
        get() {
            val qualifierType = psi.methodExpression.qualifierExpression?.type
            if (qualifierType != null) {
                return qualifierType
            }
            
            val method = resolve() ?: return null
            if (method.hasModifierProperty(PsiModifier.STATIC)) return null
            return method.containingClass?.let { PsiTypesUtil.getClassType(it) }
        }
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

    override val receiver: UExpression?
        get() = null
    
    override val receiverType: PsiType?
        get() = null
    
    override val functionReference: USimpleNameReferenceExpression?
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

    override val typeArguments: List<PsiType>
        get() = psi.classReference?.typeParameters?.toList() ?: emptyList()
    
    override val name: String?
        get() = null

    override fun resolve() = psi.resolveMethod()
}

class JavaArrayInitializerUCallExpression(
        override val psi: PsiArrayInitializerExpression,
        override val parent: UElement?
) : JavaAbstractUExpression(), UCallExpression, PsiElementBacked {
    override val functionReference: USimpleNameReferenceExpression?
        get() = null

    override val classReference: USimpleNameReferenceExpression?
        get() = null

    override val name: String?
        get() = null
    
    override val valueArgumentCount by lz { psi.initializers.size }
    override val valueArguments by lz { psi.initializers.map { JavaConverter.convertExpression(it, this) } }

    override val typeArgumentCount: Int
        get() = 0

    override val typeArguments: List<PsiType>
        get() = emptyList()

    override val kind: UastCallKind
        get() = UastCallKind.NESTED_ARRAY_INITIALIZER

    override fun resolve() = null

    override val receiver: UExpression?
        get() = null
    
    override val receiverType: PsiType?
        get() = null
}

class JavaAnnotationArrayInitializerUCallExpression(
        override val psi: PsiArrayInitializerMemberValue,
        override val parent: UElement?
) : JavaAbstractUExpression(), UCallExpression, PsiElementBacked {
    override val kind: UastCallKind
        get() = UastCallKind.NESTED_ARRAY_INITIALIZER

    override val functionReference: USimpleNameReferenceExpression?
        get() = null

    override val classReference: USimpleNameReferenceExpression?
        get() = null

    override val name: String?
        get() = null
    
    override val valueArgumentCount by lz { psi.initializers.size }
    
    override val valueArguments by lz {
        psi.initializers.map {
            JavaConverter.convertPsiElement(it, this) as? UExpression ?: UnknownJavaExpression(it, this)
        }
    }

    override val typeArgumentCount: Int 
        get() = 0

    override val typeArguments: List<PsiType>
        get() = emptyList()

    override fun resolve() = null

    override val receiver: UExpression?
        get() = null
    
    override val receiverType: PsiType?
        get() = null
}