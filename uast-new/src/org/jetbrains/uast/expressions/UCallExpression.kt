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


import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import org.jetbrains.uast.internal.acceptList
import org.jetbrains.uast.internal.log
import org.jetbrains.uast.visitor.UastVisitor

/**
 * Represents a call expression (function call, constructor call, array initializer).
 */
interface UCallExpression : UExpression, UNamed, UResolvable {
    /**
     * Returns the call kind.
     */
    val kind: UastCallKind
    
    val receiver: UExpression?
    
    val receiverType: PsiType?

    /**
     * Returns the function reference expression if the call is a function call, null otherwise.
     */
    val functionReference: USimpleNameReferenceExpression?

    /**
     * Returns the class reference if the call is a constructor call, null otherwise.
     */
    val classReference: USimpleNameReferenceExpression?

    /**
     * Returns the value argument count.
     *
     * Retrieving the argument count could be faster than getting the [valueArguments.size],
     *    because there is no need to create actual [UExpression] instances.
     */
    val valueArgumentCount: Int

    /**
     * Returns the list of value arguments.
     */
    val valueArguments: List<UExpression>

    /**
     * Returns the type argument count.
     */
    val typeArgumentCount: Int

    /**
     * Returns the function type arguments.
     */
    val typeArguments: List<PsiType>
    
    override fun resolve(): PsiMethod?

    override fun accept(visitor: UastVisitor) {
        if (visitor.visitCallExpression(this)) return
        functionReference?.accept(visitor)
        classReference?.accept(visitor)
        valueArguments.acceptList(visitor)
        visitor.afterVisitCallExpression(this)
    }

    override fun logString() = log("UFunctionCallExpression ($kind, argCount = $valueArgumentCount)", functionReference, valueArguments)
    override fun renderString(): String {
        val ref = name ?: classReference?.renderString() ?: functionReference?.renderString() ?: "<noref>"
        return ref + "(" + valueArguments.joinToString { it.renderString() } + ")"
    }
}