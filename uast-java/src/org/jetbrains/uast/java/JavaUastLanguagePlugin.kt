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
import org.jetbrains.uast.*
import org.jetbrains.uast.java.expressions.JavaUSynchronizedExpression


class JavaUastLanguagePlugin : UastLanguagePlugin {
    override val converter: UastConverter = JavaConverter
    override val visitorExtensions = emptyList<UastVisitorExtension>()

    companion object {
        @JvmStatic val INSTANCE = JavaUastLanguagePlugin()
    }
}

internal object JavaConverter : UastConverter {
    override val priority = 0

    override fun isFileSupported(name: String): Boolean {
        return name.endsWith(".java", ignoreCase = true)
    }

    override fun convert(element: Any?, parent: UElement?, expectedClass: Class<out UElement>?): UElement? {
        if (element !is PsiElement) return null
        return convertPsiElement(element, parent, expectedClass)
    }

    override fun convertWithParent(element: Any?, expectedClass: Class<out UElement>?): UElement? {
        if (element is PsiJavaFile && expectedClass.expecting<UFile>()) return JavaUFile(element)
        if (element is PsiType && expectedClass.expecting<UType>()) return convertType(element)

        if (element !is PsiElement) return null
        getCached<UElement>(element)?.let { return it }

        val parent = unwrapParents(element.parent) ?: return null
        val parentUElement = convertWithParent(parent) ?: return null
        return convertPsiElement(element, parentUElement, expectedClass)
    }

    override fun convertWithoutParent(element: Any?, expectedClass: Class<out UElement>?): UElement? {
        if (element !is PsiElement) return null
        return convertPsiElement(element, null, expectedClass)
    }

    private inline fun <reified T : UElement> getCached(element: PsiElement): T? {
        if (!element.isValid) return null
        return element.getUserData(CACHED_UELEMENT_KEY)?.get() as? T
    }

    private tailrec fun unwrapParents(parent: PsiElement?): PsiElement? = when (parent) {
        is PsiExpressionStatement -> unwrapParents(parent.parent)
        is PsiParameterList -> unwrapParents(parent.parent)
        is PsiAnnotationParameterList -> unwrapParents(parent.parent)
        else -> parent
    }

    private fun convertPsiElement(el: PsiElement, parent: UElement?, expectedClass: Class<out UElement>?): UElement? {
        getCached<UElement>(el)?.let { return it }

        with (expectedClass) {
            return when {
                el is PsiJavaFile && expecting<UFile>() -> JavaUFile(el)
                el is PsiClass && expecting<UClass>() -> JavaUClass(el, parent)
                el is PsiCodeBlock && expecting<UBlockExpression>() -> convertBlock(el, parent)
                el is PsiMethod && expecting<UFunction>() -> convertMethod(el, parent)
                el is PsiField && expecting<UVariable>() -> convertField(el, parent)
                el is PsiParameter && expecting<UVariable>() -> convertParameter(el, parent)
                el is PsiVariable && expecting<UVariable>() -> convertVariable(el, parent)
                el is PsiClassInitializer && expecting<UFunction>() -> convertInitializer(el, parent)
                el is PsiAnnotation && expecting<UAnnotation>() -> convertAnnotation(el, parent)
                el is PsiResourceExpression -> convertExpression(el.expression, parent)
                el is PsiExpression -> convertExpression(el, parent, expectedClass)
                el is PsiStatement -> convertStatement(el, parent, expectedClass)
                el is PsiIdentifier && expecting<USimpleReferenceExpression>() -> JavaUSimpleReferenceExpression(el, el.text, parent)
                el is PsiImportStatementBase && expecting<UImportStatement>() -> convertImportStatement(el, parent)
                el is PsiTypeParameter && expecting<UTypeReference>() -> convertTypeParameter(el, parent)
                el is PsiNameValuePair && expecting<UNamedExpression>() -> convertNameValue(el, parent)
                el is PsiType && expecting<UType>() -> convertType(el)
                el is PsiArrayInitializerMemberValue && expecting<UCallExpression>() ->
                    JavaAnnotationArrayInitializerUCallExpression(el, parent)
                else -> null
            }
        }
    }

    private inline fun <reified T : UElement> Class<out UElement>?.expecting(): Boolean {
        return this == null || this == T::class.java
    }

    private inline fun <reified T1: UElement, reified T2: UElement> Class<out UElement>?.expecting2(): Boolean {
        return this == null || this == T1::class.java || this == T2::class.java
    }

    internal fun convertImportStatement(importStatement: PsiImportStatementBase, parent: UElement?): UImportStatement? {
        getCached<UImportStatement>(importStatement)?.let { return it }

        return when (importStatement) {
            is PsiImportStatement -> JavaUImportStatement(importStatement, parent)
            is PsiImportStaticStatement -> JavaUStaticImportStatement(importStatement, parent)
            else -> null
        }
    }

    internal fun convertType(type: PsiType?): UType = JavaUType(type)

    internal fun convertParameter(parameter: PsiParameter, parent: UElement?) =
        getCached(parameter) ?: JavaValueParameterUVariable(parameter, parent)

    internal fun convertBlock(block: PsiCodeBlock, parent: UElement?): UBlockExpression =
        getCached(block) ?: JavaUCodeBlockExpression(block, parent)

    internal fun convertMethod(method: PsiMethod, parent: UElement?): UFunction
        = getCached(method) ?: JavaUFunction(method, parent)

    internal fun convertField(field: PsiField, parent: UElement?): UVariable
        = getCached(field) ?: JavaUVariable(field, parent)

    internal fun convertVariable(variable: PsiVariable, parent: UElement?): UVariable
        = getCached(variable) ?: JavaUVariable(variable, parent)

    internal fun convertAnnotation(annotation: PsiAnnotation, parent: UElement?): UAnnotation =
        getCached(annotation) ?: JavaUAnnotation(annotation, parent)

    internal fun convertClass(clazz: PsiClass, parent: UElement?): UClass
        = getCached(clazz) ?: JavaUClass(clazz, parent)

    internal fun convertInitializer(initializer: PsiClassInitializer, parent: UElement?): UFunction =
        getCached(initializer) ?: JavaClassInitializerUFunction(initializer, parent)

    internal fun convertTypeParameter(parameter: PsiTypeParameter, parent: UElement?): UTypeReference =
        getCached(parameter) ?: JavaParameterUTypeReference(parameter, parent)

    internal fun convertNameValue(pair: PsiNameValuePair, parent: UElement?) = UNamedExpression(pair.name.orAnonymous(), parent).apply {
        val value = pair.value
        expression = convert(value, this) as? UExpression ?: UnknownJavaExpression(value ?: pair, this)
    }

    internal fun convertReference(expression: PsiReferenceExpression, parent: UElement?): UExpression {
        return if (expression.isQualified) {
            JavaUQualifiedExpression(expression, parent)
        } else {
            val name = expression.referenceName ?: "<error name>"
            JavaUSimpleReferenceExpression(expression, name, parent)
        }
    }

    private fun convertPolyadicExpression(
        expression: PsiPolyadicExpression,
        parent: UElement?,
        i: Int
    ): UBinaryExpression {
        return if (i == 1) JavaSeparatedPolyadicUBinaryExpression(expression, parent).apply {
            leftOperand = convertExpression(expression.operands[0], this)
            rightOperand = convertExpression(expression.operands[1], this)
        } else JavaSeparatedPolyadicUBinaryExpression(expression, parent).apply {
            leftOperand = convertPolyadicExpression(expression, parent, i - 1)
            rightOperand = convertExpression(expression.operands[i], this)
        }
    }

    internal fun convertExpression(el: PsiExpression, parent: UElement?) = convertExpression(el, parent, null) ?: UastEmptyExpression

    internal fun convertExpression(el: PsiExpression, parent: UElement?, expectedClass: Class<out UElement>?): UExpression? {
        getCached<UExpression>(el)?.let { return it }

        with (expectedClass) {
            return when {
                el is PsiPolyadicExpression && expecting<UBinaryExpression>() -> convertPolyadicExpression(el, parent, el.operands.size - 1)
                el is PsiAssignmentExpression && expecting<UBinaryExpression>() -> JavaUAssignmentExpression(el, parent)
                el is PsiConditionalExpression && expecting<UIfExpression>() -> JavaUTernaryIfExpression(el, parent)

                el is PsiNewExpression && el.anonymousClass != null && expecting<UObjectLiteralExpression>() ->
                    JavaUObjectLiteralExpression(el, parent)
                el is PsiNewExpression && expecting<UCallExpression>() -> JavaConstructorUCallExpression(el, parent)

                el is PsiMethodCallExpression && el.methodExpression.qualifierExpression != null && expecting<UQualifiedExpression>() -> {
                    JavaUCompositeQualifiedExpression(el, parent).apply {
                        receiver = convertExpression(el.methodExpression.qualifierExpression!!, this)
                        selector = JavaUCallExpression(el, this)
                    }
                }
                el is PsiMethodCallExpression && expecting<UCallExpression>() -> JavaUCallExpression(el, parent)

                el is PsiArrayInitializerExpression && expecting<UCallExpression>() -> JavaArrayInitializerUCallExpression(el, parent)
                el is PsiBinaryExpression && expecting<UBinaryExpression>() -> JavaUBinaryExpression(el, parent)
                el is PsiParenthesizedExpression && expecting<UParenthesizedExpression>() -> JavaUParenthesizedExpression(el, parent)
                el is PsiPrefixExpression && expecting<UPrefixExpression>() -> JavaUPrefixExpression(el, parent)
                el is PsiPostfixExpression && expecting<UPostfixExpression>() -> JavaUPostfixExpression(el, parent)
                el is PsiLiteralExpression && expecting<ULiteralExpression>() -> JavaULiteralExpression(el, parent)
                el is PsiReferenceExpression && expecting2<USimpleReferenceExpression, UQualifiedExpression>() -> convertReference(el, parent)
                el is PsiThisExpression && expecting<UThisExpression>() -> JavaUThisExpression(el, parent)
                el is PsiSuperExpression && expecting<USuperExpression>() -> JavaUSuperExpression(el, parent)
                el is PsiInstanceOfExpression && expecting<UBinaryExpressionWithType>() -> JavaUInstanceCheckExpression(el, parent)
                el is PsiTypeCastExpression && expecting<UBinaryExpressionWithType>() -> JavaUTypeCastExpression(el, parent)
                el is PsiClassObjectAccessExpression && expecting<UClassLiteralExpression>() -> JavaUClassLiteralExpression(el, parent)
                el is PsiArrayAccessExpression && expecting<UArrayAccessExpression>() -> JavaUArrayAccessExpression(el, parent)
                el is PsiLambdaExpression && expecting<ULambdaExpression>() -> JavaULambdaExpression(el, parent)
                el is PsiMethodReferenceExpression && expecting<UCallableReferenceExpression>() -> JavaUCallableReferenceExpression(el, parent)

                expectedClass == null -> UnknownJavaExpression(el, parent)
                else -> null
            }
        }
    }

    internal fun convertStatement(el: PsiStatement, parent: UElement?) = convertStatement(el, parent, null) ?: UastEmptyExpression

    internal fun convertStatement(el: PsiStatement, parent: UElement?, expectedClass: Class<out UElement>?): UExpression? {
        getCached<UExpression>(el)?.let { return it }

        with (expectedClass) {
            return when {
                el is PsiDeclarationStatement && expecting<UDeclarationsExpression>() -> convertDeclarations(el.declaredElements, parent)
                el is PsiExpressionListStatement && expecting<UDeclarationsExpression>() ->
                    convertDeclarations(el.expressionList.expressions, parent)
                el is PsiBlockStatement && expecting<UBlockExpression>() -> JavaUBlockExpression(el, parent)
                el is PsiLabeledStatement && expecting<ULabeledExpression>() -> JavaULabeledExpression(el, parent)
                el is PsiExpressionStatement -> convertExpression(el.expression, parent, expectedClass)
                el is PsiIfStatement && expecting<UIfExpression>() -> JavaUIfExpression(el, parent)
                el is PsiSwitchStatement && expecting<USwitchExpression>() -> JavaUSwitchExpression(el, parent)
                el is PsiSwitchLabelStatement && expecting<USwitchClauseExpression>() -> {
                    if (el.isDefaultCase)
                        DefaultUSwitchClauseExpression(parent)
                    else JavaUCaseSwitchClauseExpression(el, parent)
                }
                el is PsiWhileStatement && expecting<UWhileExpression>() -> JavaUWhileExpression(el, parent)
                el is PsiDoWhileStatement && expecting<UDoWhileExpression>() -> JavaUDoWhileExpression(el, parent)
                el is PsiForStatement && expecting<UForExpression>() -> JavaUForExpression(el, parent)
                el is PsiForeachStatement && expecting<UForEachExpression>() -> JavaUForEachExpression(el, parent)
                el is PsiBreakStatement && expecting<UBreakExpression>() -> JavaUBreakExpression(el, parent)
                el is PsiContinueStatement && expecting<UContinueExpression>() -> JavaUContinueExpression(el, parent)
                el is PsiReturnStatement && expecting<UReturnExpression>() -> JavaUReturnExpression(el, parent)
                el is PsiAssertStatement && expecting<UCallExpression>() -> JavaUAssertExpression(el, parent)
                el is PsiThrowStatement && expecting<UThrowExpression>() -> JavaUThrowExpression(el, parent)
                el is PsiSynchronizedStatement && expecting<UBlockExpression>() -> JavaUSynchronizedExpression(el, parent)
                el is PsiTryStatement && expecting<UTryExpression>() -> JavaUTryExpression(el, parent)

                expectedClass == null -> UnknownJavaExpression(el, parent)
                else -> null
            }
        }
    }

    internal fun convertOrEmpty(statement: PsiStatement?, parent: UElement?): UExpression {
        return statement?.let { convertStatement(it, parent, null) } ?: UastEmptyExpression
    }

    internal fun convertOrEmpty(expression: PsiExpression?, parent: UElement?): UExpression {
        return expression?.let { convertExpression(it, parent, null) } ?: UastEmptyExpression
    }

    internal fun convertOrNull(expression: PsiExpression?, parent: UElement?): UExpression? {
        return if (expression != null) convertExpression(expression, parent, null) else null
    }

    internal fun convertOrEmpty(block: PsiCodeBlock?, parent: UElement?): UExpression {
        return if (block != null) convertBlock(block, parent) else UastEmptyExpression
    }

    private fun convertDeclarations(elements: Array<out PsiElement>, parent: UElement?): SimpleUDeclarationsExpression {
        val uelements = arrayListOf<UElement>()
        return SimpleUDeclarationsExpression(parent, uelements).apply {
            for (element in elements) {
                convert(element, this)?.let { uelements += it }
            }
        }
    }
}