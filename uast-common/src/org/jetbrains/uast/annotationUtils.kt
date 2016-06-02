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

@file:JvmMultifileClass
@file:JvmName("UastUtils")
package org.jetbrains.uast

/**
 * Find an annotation with the required qualified name.
 *
 * @param fqName the qualified name to search
 * @return [UAnnotation] element if the annotation with the specified [fqName] was found, null otherwise.
 */
fun UAnnotated.findAnnotation(fqName: String) = annotations.firstOrNull { it.fqName == fqName }

fun UDeclaration.getAllAnnotations(context: UastContext) : List<UAnnotation> {
    val annotations = mutableListOf<UAnnotation>()
    if (this is UAnnotated) {
        annotations += this.annotations
    }

    for (overridden in this.getOverriddenDeclarations(context)) {
        if (overridden is UAnnotated) {
            annotations += overridden.annotations
        }
    }
    return annotations
}