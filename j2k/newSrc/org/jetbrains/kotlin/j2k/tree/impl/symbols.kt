/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.tree.impl

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiVariable
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.j2k.JKSymbolProvider
import org.jetbrains.kotlin.j2k.ast.Nullability
import org.jetbrains.kotlin.j2k.conversions.parentOfType
import org.jetbrains.kotlin.j2k.conversions.resolveFqName
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtNamedFunction

interface JKSymbol {
    val target: Any
    val declaredIn: JKSymbol?
    val fqName: String?
}

interface JKUnresolvedSymbol : JKSymbol

fun JKSymbol.isUnresolved() =
    this is JKUnresolvedSymbol

interface JKNamedSymbol : JKSymbol {
    val name: String
}

interface JKUniverseSymbol<T : JKTreeElement> : JKSymbol {
    override var target: T
}

interface JKClassSymbol : JKNamedSymbol


interface JKMethodSymbol : JKNamedSymbol {
    override val fqName: String
    val receiverType: JKType?
    val parameterTypes: List<JKType>?
    val returnType: JKType?
}

fun JKMethodSymbol.parameterTypesWithUnfoldedVarargs(): Sequence<JKType>? {
    val realParameterTypes = parameterTypes ?: return null
    if (realParameterTypes.isEmpty()) return emptySequence()
    val lastArrayType = realParameterTypes.last().arrayInnerType() ?: return realParameterTypes.asSequence()
    return realParameterTypes.dropLast(1).asSequence() + generateSequence { lastArrayType }
}


interface JKFieldSymbol : JKNamedSymbol {
    override val fqName: String
    val fieldType: JKType?
}

class JKUniverseClassSymbol : JKClassSymbol, JKUniverseSymbol<JKClass> {
    override lateinit var target: JKClass
    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String
        get() = target.name.value // TODO("Fix this")
    override val name: String
        get() = target.name.value
}

class JKMultiverseClassSymbol(override val target: PsiClass) : JKClassSymbol {

    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String?
        get() = target.qualifiedName

    override val name: String
        get() = target.name!!
}

class JKMultiverseKtClassSymbol(override val target: KtClassOrObject) : JKClassSymbol {
    override val name: String
        get() = target.name!!
    override val declaredIn: JKSymbol
        get() = TODO("not implemented")
    override val fqName: String?
        get() = target.fqName?.asString()

}

fun JKClassSymbol.displayName() =
    when (this) {
        is JKUniverseClassSymbol ->
            target.psi<PsiClass>()
                ?.let { it.nameWithOuterClasses() }
                ?: name
        is JKMultiverseClassSymbol -> target.nameWithOuterClasses()
        else -> name
    }

fun PsiClass.nameWithOuterClasses() =
    generateSequence(this) { it.containingClass }
        .toList()
        .reversed()
        .joinToString(separator = ".") { it.name!! }

class JKUniverseMethodSymbol(private val symbolProvider: JKSymbolProvider) : JKMethodSymbol, JKUniverseSymbol<JKMethod> {
    override val receiverType: JKType?
        get() = (target.parent as? JKClass)?.let {
            JKClassTypeImpl(symbolProvider.provideUniverseSymbol(it), emptyList()/*TODO*/)
        }
    override val parameterTypes: List<JKType>
        get() = target.parameters.map { it.type.type }
    override val returnType: JKType
        get() = target.returnType.type
    override val name: String
        get() = target.name.value
    override lateinit var target: JKMethod
    override val declaredIn: JKSymbol?
        get() = target.parentOfType<JKClass>()?.let { symbolProvider.provideUniverseSymbol(it) }
    override val fqName: String
        get() = target.name.value // TODO("Fix this")
}

class JKMultiverseMethodSymbol(override val target: PsiMethod, private val symbolProvider: JKSymbolProvider) : JKMethodSymbol {
    override val receiverType: JKType?
        get() = target.containingClass?.let {
            JKClassTypeImpl(symbolProvider.provideDirectSymbol(it) as JKClassSymbol, emptyList()/*TODO*/)
        }
    override val parameterTypes: List<JKType>
        get() = target.parameterList.parameters.map { it.type.toJK(symbolProvider) }
    override val returnType: JKType
        get() = target.returnType!!.toJK(symbolProvider)
    override val name: String
        get() = target.name
    override val declaredIn: JKSymbol?
        get() = target.containingClass?.let { symbolProvider.provideDirectSymbol(it) }
    override val fqName: String
        get() = target.name // TODO("Fix this")
}

class JKMultiverseFunctionSymbol(override val target: KtNamedFunction, private val symbolProvider: JKSymbolProvider) : JKMethodSymbol {
    override val receiverType: JKType?
        get() = target.receiverTypeReference?.typeElement?.toJK(symbolProvider)
    override val parameterTypes: List<JKType>?
        get() = target.valueParameters.map { parameter ->
            val type = parameter.typeReference?.typeElement?.toJK(symbolProvider)
            type?.let {
                if (parameter.isVarArg) {
                    JKClassTypeImpl(
                        symbolProvider.provideByFqName(KotlinBuiltIns.FQ_NAMES.array),
                        listOf(it)
                    )
                } else it
            }
        }.takeIf { parameters -> parameters.all { it != null } } as? List<JKType>

    override val returnType: JKType?
        get() = target.typeReference?.typeElement?.toJK(symbolProvider)
    override val name: String
        get() = target.name!!
    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String
        get() = target.fqName!!.asString()
}

class JKUniverseFieldSymbol : JKFieldSymbol, JKUniverseSymbol<JKVariable> {
    override val fieldType: JKType
        get() = target.type.type
    override val name: String
        get() = target.name.value
    override lateinit var target: JKVariable
    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String
        get() = target.name.value // TODO("Fix this")
}

class JKMultiverseFieldSymbol(override val target: PsiVariable, private val symbolProvider: JKSymbolProvider) : JKFieldSymbol {
    override val fieldType: JKType
        get() = target.type.toJK(symbolProvider)
    override val name: String
        get() = target.name!!
    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String
        get() = target.getKotlinFqName()?.asString() ?: target.name!!
}

class JKMultiversePropertySymbol(override val target: KtCallableDeclaration, private val symbolProvider: JKSymbolProvider) : JKFieldSymbol {
    override val fieldType: JKType?
        get() = target.typeReference?.typeElement?.toJK(symbolProvider)
    override val name: String
        get() = target.name!!
    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String
        get() = target.fqName!!.asString()
}

class JKUnresolvedField(override val target: PsiReference, private val symbolProvider: JKSymbolProvider) : JKFieldSymbol, JKUnresolvedSymbol {
    override val fieldType: JKType
        get() {
            val resolvedType = (target as? PsiReferenceExpressionImpl)?.type
            if (resolvedType != null) return resolvedType.toJK(symbolProvider)

            val nothingSymbol = (symbolProvider.provideDirectSymbol(
                resolveFqName(ClassId.fromString("kotlin.Nothing"), symbolProvider.symbolsByPsi.keys.first())!!
            ) as JKClassSymbol)
            return JKClassTypeImpl(nothingSymbol, emptyList())
        }
    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String = target.canonicalText
    override val name: String = target.canonicalText
}

class JKUnresolvedMethod(
    override val target: String,
    override val returnType: JKType = JKNoTypeImpl
) : JKMethodSymbol, JKUnresolvedSymbol {
    constructor(target: PsiReference) : this(target.canonicalText)

    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String = target
    override val receiverType: JKType?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val parameterTypes: List<JKType>
        get() = TODO(target) //To change initializer of created properties use File | Settings | File Templates.
    override val name: String
        get() = target
}

class JKExclExclMethod(
    operandType: JKType
) : JKMethodSymbol {
    override val target: String = "!!"
    override val returnType: JKType = operandType.updateNullability(Nullability.NotNull)
    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String = "!!"
    override val receiverType: JKType?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val parameterTypes: List<JKType>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val name: String = "!!"
}

class JKUnresolvedClassSymbol(override val target: String) : JKClassSymbol, JKUnresolvedSymbol {
    override val declaredIn: JKSymbol
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val fqName: String?
        get() = target

    override val name: String
        get() = target.substringAfterLast('.')
}


fun JKSymbol.deepestFqName(): String? {
    fun Any.deepestFqNameForTarget(): String? =
        when (this) {
            is PsiMethod -> (findDeepestSuperMethods().firstOrNull() ?: this).getKotlinFqName()?.asString()
            is KtNamedFunction -> findDeepestSuperMethodsNoWrapping(this).firstOrNull()?.getKotlinFqName()?.asString()
            is JKMethod -> psi()?.deepestFqNameForTarget()
            else -> null
        }
    return target.deepestFqNameForTarget() ?: fqName
}