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

import com.intellij.psi.PsiAnnotation
import org.jetbrains.uast.*
import org.jetbrains.uast.java.internal.getUastValue
import org.jetbrains.uast.psi.PsiElementBacked

class JavaUAnnotation(
        override val psi: PsiAnnotation,
        override val parent: UElement?
) : JavaAbstractUElement(), UAnnotation, PsiElementBacked {
    override val name: String
        get() = psi.nameReferenceElement?.referenceName.orAnonymous()

    override val fqName: String?
        get() = psi.qualifiedName

    override val valueArguments by lz {
        psi.parameterList.attributes.map {
            JavaConverter.convertNameValue(it, this)
        }
    }

    override val valueArgumentsCount: Int
        get() = psi.parameterList.attributes.size

    override val nameElement by lz { JavaDumbUElement(psi.nameReferenceElement, this) }

    override fun getValue(name: String?): UConstantValue<*>? {
        val attributes = psi.parameterList.attributes
        val value = if (name == null) psi.findAttributeValue(null) else attributes.firstOrNull { it.name == name }?.value
        return value?.getUastValue(psi.project)
    }

    override fun getValues(): Map<String, UConstantValue<*>> {
        val values = mutableMapOf<String, UConstantValue<*>>()
        psi.parameterList.attributes.forEachIndexed { i, pair ->
            values.put(pair.name ?: "p$i", pair.value.getUastValue(psi.project))
        }
        return values
    }

    override fun resolve(context: UastContext) = context.convert(psi.nameReferenceElement?.resolve()) as? UClass
}