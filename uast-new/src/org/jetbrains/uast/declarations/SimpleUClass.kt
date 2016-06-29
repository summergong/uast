package org.jetbrains.uast

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement

class SimpleUClass(
        override val psi: PsiClass, 
        override val languagePlugin: UastLanguagePlugin, 
        override val parent: UElement?
) : UClass, PsiClass by psi {
    override val uastDeclarations by lz {
        mutableListOf<UDeclaration>().apply {
            addAll(uastFields)
            addAll(uastInitializers)
            addAll(uastMethods)
            addAll(uastNestedClasses)
        }
    }
    
    override val uastFields: List<UVariable> by lz { psi.fields.map { SimpleUVariable(it, languagePlugin, this) } }
    override val uastInitializers: List<UInitializer> by lz { psi.initializers.map { SimpleUInitializer(it, languagePlugin, this) } }
    override val uastMethods: List<UMethod> by lz { psi.methods.map { SimpleUMethod(it, languagePlugin, this) } }
    override val uastNestedClasses: List<UClass> by lz { psi.innerClasses.map { SimpleUClass(it, languagePlugin, this) } }
}