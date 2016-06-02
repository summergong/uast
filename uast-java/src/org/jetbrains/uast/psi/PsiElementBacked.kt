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
@file:JvmName("UastPsiUtils")
package org.jetbrains.uast.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UElement
import java.util.*

interface PsiElementBacked : UElement {
    val psi: PsiElement?

    override val comments: List<String>
        get() = psi?.children?.fold(ArrayList<String>(0)) { list, item ->
            if (item is PsiComment) {
                list += item.text
            }
            list
        } ?: emptyList()

    override val isValid: Boolean
        get() = psi?.isValid ?: true
}