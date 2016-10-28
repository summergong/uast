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

import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiSwitchLabelStatement
import com.intellij.psi.PsiSwitchStatement
import com.intellij.psi.impl.source.tree.ChildRole
import org.jetbrains.uast.*
import org.jetbrains.uast.internal.log
import org.jetbrains.uast.java.expressions.JavaUExpressionList
import org.jetbrains.uast.java.kinds.JavaSpecialExpressionKinds
import org.jetbrains.uast.psi.PsiElementBacked

class JavaUSwitchExpression(
        override val psi: PsiSwitchStatement,
        override val containingElement: UElement?
) : JavaAbstractUExpression(), USwitchExpression, PsiElementBacked {
    override val expression by lz { JavaConverter.convertOrEmpty(psi.expression, this) }

    override val body: UExpressionList by lz {
        object : JavaUExpressionList(psi, JavaSpecialExpressionKinds.SWITCH, this) {
            override fun asRenderString() = expressions.joinToString("\n") { it.asRenderString().withMargin }
        }.apply {
            expressions = this@JavaUSwitchExpression.psi.body?.convertToSwitchEntryList(this) ?: emptyList()
        }
    }


    override val switchIdentifier: UIdentifier
        get() = UIdentifier(psi.getChildByRole(ChildRole.SWITCH_KEYWORD), this)
}

private fun PsiCodeBlock.convertToSwitchEntryList(containingElement: UExpression): List<JavaUSwitchEntry> {
    var currentLabels = listOf<PsiSwitchLabelStatement>()
    var currentBody = listOf<PsiStatement>()
    val result = mutableListOf<JavaUSwitchEntry>()
    for (statement in statements) {
        if (statement is PsiSwitchLabelStatement) {
            if (currentBody.isEmpty()) {
                currentLabels += statement
            }
            else if (currentLabels.isNotEmpty()) {
                result += JavaUSwitchEntry(currentLabels, currentBody, containingElement)
                currentLabels = listOf(statement)
                currentBody = listOf<PsiStatement>()
            }
        }
        else {
            currentBody += statement
        }
    }
    if (currentLabels.isNotEmpty()) {
        result += JavaUSwitchEntry(currentLabels, currentBody, containingElement)
    }
    return result
}

class JavaUSwitchEntry(
        val labels: List<PsiSwitchLabelStatement>,
        val statements: List<PsiStatement>,
        override val containingElement: UExpression
) : JavaAbstractUExpression(), USwitchClauseExpressionWithBody, PsiElementBacked {
    override val psi: PsiSwitchLabelStatement = labels.first()

    override val caseValues by lz {
        labels.map {
            if (it.isDefaultCase) {
                listOf(JavaUDefaultCaseExpression)
            }
            else {
                val value = it.caseValue
                value?.let { listOf(JavaConverter.convertExpression(it, this)) } ?: emptyList()
            }
        }.flatten()
    }

    override val body: UExpression by lz {
        object : JavaUExpressionList(psi, JavaSpecialExpressionKinds.SWITCH_ENTRY, this) {
            override fun asRenderString() = buildString {
                appendln("{")
                expressions.forEach { appendln(it.asRenderString().withMargin) }
                appendln("}")
            }
        }.apply {
            val statements = this@JavaUSwitchEntry.statements
            expressions = statements.map { JavaConverter.convertOrEmpty(it, this) }
        }
    }
}

object JavaUDefaultCaseExpression : UExpression {
    override val containingElement: UElement?
        get() = null

    override val annotations: List<UAnnotation>
        get() = emptyList()

    override fun asLogString() = "UDefaultCaseExpression"

    override fun asRenderString() = "else"
}