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

package org.jetbrains.uast.java.internal

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import org.jetbrains.uast.*
import org.jetbrains.uast.expressions.UastExpressionFactory
import org.jetbrains.uast.java.JavaConverter

internal fun PsiAnnotationMemberValue?.getUastValue(project: Project): UConstantValue<*> {
    val originalFactory = UastExpressionFactory { JavaConverter.convertWithoutParent(this) as? UExpression }

    if (this == null) return UErrorValue(originalFactory)

    if (this is PsiLiteralExpression && this.type == PsiType.NULL) {
        return UNullValue(originalFactory)
    }

    fun computeConstantExpression() = JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(this)

    val literalValue = (this as? PsiLiteralExpression)?.value ?: computeConstantExpression()

    if (literalValue != null) {
        when (literalValue) {
            is Boolean -> return UBooleanValue(literalValue, originalFactory)
            is Double -> return UDoubleValue(literalValue, originalFactory)
            is Float -> return UFloatValue(literalValue, originalFactory)
            is String -> return UStringValue(literalValue, originalFactory)
            is Char -> return UCharValue(literalValue, originalFactory)
            is Byte -> return UByteValue(literalValue, originalFactory)
            is Short -> return UShortValue(literalValue, originalFactory)
            is Int -> return UIntValue(literalValue, originalFactory)
            is Long -> return ULongValue(literalValue, originalFactory)
        }
    }

    return when (this) {
        is PsiReferenceExpression -> {
            val element = resolve()
            if (element is PsiEnumConstant) {
                UEnumValue(null, JavaConverter.convertType(element.type), element.name ?: "<error>", originalFactory)
            }
            else {
                UErrorValue(originalFactory)
            }
        }
        is PsiArrayInitializerMemberValue -> UArrayValue(initializers.map { it.getUastValue(project) }, originalFactory)
        is PsiAnnotation -> UAnnotationValue(JavaConverter.convertAnnotation(this, null), originalFactory)
        is PsiClassObjectAccessExpression -> UTypeValue(JavaConverter.convertType(type), originalFactory)
        else -> throw UnsupportedOperationException("Unsupported annotation this type: " + this)
    }
}