package org.jetbrains.uast

import com.intellij.psi.*
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.UastLanguagePlugin
import org.jetbrains.uast.expressions.UReferenceExpression

abstract class AbstractUVariable : PsiVariable, UVariable {
    override val uastInitializer by lz { languagePlugin.getInitializerBody(psi) }
    
    override fun equals(other: Any?) = psi.equals(other)
    override fun hashCode() = psi.hashCode()
}

class SimpleUVariable(
        override val psi: PsiVariable, 
        override val languagePlugin: UastLanguagePlugin, 
        override val containingElement: UElement?
) : AbstractUVariable(), UVariable, PsiVariable by psi {
    companion object {
        fun create(psi: PsiVariable, languagePlugin: UastLanguagePlugin, containingElement: UElement?): UVariable {
            return when (psi) {
                is PsiEnumConstant -> SimpleUEnumConstant(psi, languagePlugin, containingElement)
                is PsiLocalVariable -> SimpleULocalVariable(psi, languagePlugin, containingElement)
                is PsiParameter -> SimpleUParameter(psi, languagePlugin, containingElement)
                is PsiField -> SimpleUField(psi, languagePlugin, containingElement)
                else -> SimpleUVariable(psi, languagePlugin, containingElement)
            }
        }
    }
}

class SimpleUParameter(
        override val psi: PsiParameter,
        override val languagePlugin: UastLanguagePlugin,
        override val containingElement: UElement?
) : AbstractUVariable(), UParameter, PsiParameter by psi

class SimpleUField(
        override val psi: PsiField,
        override val languagePlugin: UastLanguagePlugin,
        override val containingElement: UElement?
) : AbstractUVariable(), UField, PsiField by psi

class SimpleULocalVariable(
        override val psi: PsiLocalVariable,
        override val languagePlugin: UastLanguagePlugin,
        override val containingElement: UElement?
) : AbstractUVariable(), ULocalVariable, PsiLocalVariable by psi

class SimpleUEnumConstant(
        override val psi: PsiEnumConstant,
        override val languagePlugin: UastLanguagePlugin,
        override val containingElement: UElement?
) : AbstractUVariable(), UEnumConstant, PsiEnumConstant by psi {
    override val kind: UastCallKind
        get() = UastCallKind.CONSTRUCTOR_CALL
    override val receiver: UExpression?
        get() = null
    override val receiverType: PsiType?
        get() = null
    override val methodReference: UReferenceExpression?
        get() = null
    override val classReference: UReferenceExpression?
        get() = null
    override val typeArgumentCount: Int
        get() = 0
    override val typeArguments: List<PsiType>
        get() = emptyList()
    override val valueArgumentCount: Int
        get() = psi.argumentList?.expressions?.size ?: 0

    override val valueArguments by lz {
        psi.argumentList?.expressions?.map { 
            languagePlugin.convert(it, this) as? UExpression ?: UastEmptyExpression 
        } ?: emptyList()
    }

    override val returnType: PsiType?
        get() = psi.type

    override fun resolve() = psi.resolveMethod()

    override val methodName: String?
        get() = null
}
