package org.jetbrains.uast

import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement

abstract class AbstractUClass : UClass {
    override val uastDeclarations by lz {
        mutableListOf<UDeclaration>().apply {
            addAll(uastFields)
            addAll(uastInitializers)
            addAll(uastMethods)
            addAll(uastNestedClasses)
        }
    }
    
    override val uastFields: List<UVariable> by lz { psi.fields.map { SimpleUVariable.create(it, languagePlugin, this) } }
    override val uastInitializers: List<UClassInitializer> by lz { psi.initializers.map { SimpleUClassInitializer(it, languagePlugin, this) } }
    override val uastMethods: List<UMethod> by lz { psi.methods.map { SimpleUMethod(it, languagePlugin, this) } }
    override val uastNestedClasses: List<UClass> by lz { psi.innerClasses.map { SimpleUClass.create(it, languagePlugin, this) } }

    override fun equals(other: Any?) = psi.equals(other)
    override fun hashCode() = psi.hashCode()
}

class SimpleUClass private constructor(
        override val psi: PsiClass, 
        override val languagePlugin: UastLanguagePlugin, 
        override val containingElement: UElement?
) : AbstractUClass(), PsiClass by psi {
    companion object {
        fun create(psi: PsiClass, languagePlugin: UastLanguagePlugin, containingElement: UElement?): UClass {
            return if (psi is PsiAnonymousClass) 
                SimpleUAnonymousClass(psi, languagePlugin, containingElement)
            else
                SimpleUClass(psi, languagePlugin, containingElement)
        }
    }
}

class SimpleUAnonymousClass(
        override val psi: PsiAnonymousClass,
        override val languagePlugin: UastLanguagePlugin,
        override val containingElement: UElement?
) : AbstractUClass(), UAnonymousClass, PsiAnonymousClass by psi