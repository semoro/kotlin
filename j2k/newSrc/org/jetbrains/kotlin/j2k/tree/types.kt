/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.tree

import com.intellij.psi.*
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.util.getJavaClassDescriptor
import org.jetbrains.kotlin.j2k.ConversionContext
import org.jetbrains.kotlin.j2k.JKSymbolProvider
import org.jetbrains.kotlin.j2k.ast.ArrayType
import org.jetbrains.kotlin.j2k.ast.Nullability
import org.jetbrains.kotlin.j2k.conversions.resolveFqName
import org.jetbrains.kotlin.j2k.kotlinTypeByName
import org.jetbrains.kotlin.j2k.tree.impl.*
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

fun JKExpression.type(symbolProvider: JKSymbolProvider): JKType? =
    when (this) {
        is JKLiteralExpression -> type.toJkType(symbolProvider)
        is JKOperatorExpression -> {
            (operator as? JKKtOperatorImpl)?.returnType
                ?: error("Cannot get type of ${operator::class}, it should be first converted to KtOperator")

        }
        is JKMethodCallExpression -> identifier.returnType
        is JKFieldAccessExpressionImpl -> identifier.fieldType
        is JKQualifiedExpressionImpl -> this.selector.type(symbolProvider)
        is JKKtThrowExpression -> kotlinTypeByName(KotlinBuiltIns.FQ_NAMES.nothing.asString(), symbolProvider)
        is JKClassAccessExpression -> null
        is JKJavaNewExpression -> JKClassTypeImpl(classSymbol)
        is JKKtIsExpression -> kotlinTypeByName(KotlinBuiltIns.FQ_NAMES._boolean.asString(), symbolProvider)
        is JKParenthesizedExpression -> expression.type(symbolProvider)
        is JKTypeCastExpression -> type.type
        is JKThisExpression -> null// TODO return actual type
        is JKSuperExpression -> null// TODO return actual type
        is JKStubExpression -> null
        is JKIfElseExpression -> thenBranch.type(symbolProvider)// TODO return actual type
        is JKArrayAccessExpression ->
            (expression.type(symbolProvider) as? JKParametrizedType)?.parameters?.lastOrNull()
        is JKClassLiteralExpression -> {
            val symbol = when (literalType) {
                JKClassLiteralExpression.LiteralType.KOTLIN_CLASS ->
                    symbolProvider.provideByFqName<JKClassSymbol>("kotlin.reflect.KClass")
                JKClassLiteralExpression.LiteralType.JAVA_CLASS,
                JKClassLiteralExpression.LiteralType.JAVA_PRIMITIVE_CLASS, JKClassLiteralExpression.LiteralType.JAVA_VOID_TYPE ->
                    symbolProvider.provideByFqName("java.lang.Class")
            }
            JKClassTypeImpl(symbol, listOf(classType.type), Nullability.NotNull)
        }
        is JKKtAnnotationArrayInitializerExpression -> JKNoTypeImpl //TODO
        is JKLambdaExpression -> returnType.type
        else -> TODO(this::class.java.toString())
    }

fun ClassId.toKtClassType(
    symbolProvider: JKSymbolProvider,
    nullability: Nullability = Nullability.Default,
    context: PsiElement = symbolProvider.symbolsByPsi.keys.first()
): JKType {
    val typeSymbol = symbolProvider.provideDirectSymbol(resolveFqName(this, context)!!) as JKClassSymbol
    return JKClassTypeImpl(typeSymbol, emptyList(), nullability)
}

fun PsiType.toJK(symbolProvider: JKSymbolProvider, nullability: Nullability = Nullability.Default): JKType {
    return when (this) {
        is PsiClassType -> {
            val target = resolve()
            val parameters = parameters.map { it.toJK(symbolProvider, nullability) }
            when (target) {
                null ->
                    JKClassTypeImpl(JKUnresolvedClassSymbol(rawType().canonicalText), parameters, nullability)
                is PsiTypeParameter ->
                    JKTypeParameterTypeImpl(target.name!!)
                else -> {
                    JKClassTypeImpl(
                        target.let { symbolProvider.provideDirectSymbol(it) as JKClassSymbol },
                        parameters,
                        nullability
                    )
                }
            }
        }
        is PsiArrayType -> JKJavaArrayTypeImpl(componentType.toJK(symbolProvider, nullability), nullability)
        is PsiPrimitiveType -> JKJavaPrimitiveTypeImpl.KEYWORD_TO_INSTANCE[presentableText]
            ?: error("Invalid primitive type $presentableText")
        is PsiDisjunctionType ->
            JKJavaDisjunctionTypeImpl(disjunctions.map { it.toJK(symbolProvider) })
        is PsiWildcardType ->
            when {
                isExtends ->
                    JKVarianceTypeParameterTypeImpl(
                        JKVarianceTypeParameterType.Variance.OUT,
                        extendsBound.toJK(symbolProvider)
                    )
                isSuper ->
                    JKVarianceTypeParameterTypeImpl(
                        JKVarianceTypeParameterType.Variance.IN,
                        superBound.toJK(symbolProvider)
                    )
                else -> JKStarProjectionTypeImpl()
            }
        else -> throw Exception("Invalid PSI ${this::class.java}")
    }
}

fun JKClassSymbol.asType(nullability: Nullability = Nullability.Default): JKClassType =
    JKClassTypeImpl(this, emptyList(), nullability)

fun JKType.isSubtypeOf(other: JKType, symbolProvider: JKSymbolProvider): Boolean =
    other.toKtType(symbolProvider)
        ?.let { otherType -> this.toKtType(symbolProvider)?.isSubtypeOf(otherType) } == true



fun KotlinType.toJK(symbolProvider: JKSymbolProvider): JKClassTypeImpl =
    JKClassTypeImpl(
        symbolProvider.provideByFqName(getJetTypeFqName(false)),
        arguments.map { it.type.toJK(symbolProvider) },
        if (isNullable()) Nullability.Nullable else Nullability.NotNull
    )


fun KtTypeReference.toJK(symbolProvider: JKSymbolProvider): JKType? =
    analyze()
        .get(BindingContext.TYPE, this)
        ?.toJK(symbolProvider)


fun JKType.toKtType(symbolProvider: JKSymbolProvider): KotlinType? =
    when (this) {
        is JKClassType -> classReference.toKtType()
        is JKJavaPrimitiveType ->
            kotlinTypeByName(
                jvmPrimitiveType.primitiveType.typeFqName.asString(),
                symbolProvider
            ).toKtType(symbolProvider)
        else -> null
//        else -> TODO(this::class.java.toString())
    }


fun JKClassSymbol.toKtType(): KotlinType? {
    val classDescriptor = when (this) {
        is JKMultiverseKtClassSymbol -> {
            val bindingContext = target.analyze()
            bindingContext[BindingContext.DECLARATION_TO_DESCRIPTOR, target] as ClassDescriptor
        }
        is JKMultiverseClassSymbol ->
            target.getJavaClassDescriptor()
        is JKUniverseClassSymbol ->
            target.psi<PsiClass>()?.getJavaClassDescriptor()//TODO null in case of a fake package
        else -> TODO(this::class.java.toString())
    }
    return classDescriptor?.defaultType
}

fun JKType.applyRecursive(transform: (JKType) -> JKType?): JKType =
    transform(this) ?: when (this) {
        is JKTypeParameterTypeImpl -> this
        is JKClassTypeImpl ->
            JKClassTypeImpl(
                classReference,
                parameters.map { it.applyRecursive(transform) },
                nullability
            )
        is JKNoType -> this
        is JKJavaVoidType -> this
        is JKJavaPrimitiveType -> this
        is JKJavaArrayType -> JKJavaArrayTypeImpl(type.applyRecursive(transform), nullability)
        is JKContextType -> JKContextType
        is JKJavaDisjunctionType ->
            JKJavaDisjunctionTypeImpl(disjunctions.map { it.applyRecursive(transform) }, nullability)
        is JKStarProjectionType -> this
        else -> TODO(this::class.toString())
    }

inline fun <reified T : JKType> T.updateNullability(newNullability: Nullability): T =
    if (nullability == newNullability) this
    else when (this) {
        is JKTypeParameterTypeImpl -> JKTypeParameterTypeImpl(name, newNullability)
        is JKClassTypeImpl -> JKClassTypeImpl(classReference, parameters, newNullability)
        is JKNoType -> this
        is JKJavaVoidType -> this
        is JKJavaPrimitiveType -> this
        is JKJavaArrayType -> JKJavaArrayTypeImpl(type, newNullability)
        is JKContextType -> JKContextType
        is JKJavaDisjunctionType -> this
        else -> TODO(this::class.toString())
    } as T

fun <T : JKType> T.updateNullabilityRecursively(newNullability: Nullability): T =
    applyRecursive {
        when (it) {
            is JKTypeParameterTypeImpl -> JKTypeParameterTypeImpl(it.name, newNullability)
            is JKClassTypeImpl ->
                JKClassTypeImpl(
                    it.classReference,
                    it.parameters.map { it.updateNullabilityRecursively(newNullability) },
                    newNullability
                )
            is JKJavaArrayType -> JKJavaArrayTypeImpl(it.type.updateNullabilityRecursively(newNullability), newNullability)
            else -> null
        }
    } as T

fun JKJavaMethod.returnTypeNullability(context: ConversionContext): Nullability =
    context.typeFlavorCalculator.methodNullability(psi()!!)

fun JKType.isCollectionType(symbolProvider: JKSymbolProvider): Boolean {
    if (this !is JKClassType) return false
    val collectionType = JKClassTypeImpl(symbolProvider.provideByFqName("java.util.Collection"), emptyList())
    return this.isSubtypeOf(collectionType, symbolProvider)
}

fun JKType.isStringType(): Boolean =
    (this as? JKClassType)?.classReference?.name == "String"

fun JKLiteralExpression.LiteralType.toPrimitiveType(): JKJavaPrimitiveType? =
    when (this) {
        JKLiteralExpression.LiteralType.CHAR -> JKJavaPrimitiveTypeImpl.CHAR
        JKLiteralExpression.LiteralType.BOOLEAN -> JKJavaPrimitiveTypeImpl.BOOLEAN
        JKLiteralExpression.LiteralType.INT -> JKJavaPrimitiveTypeImpl.INT
        JKLiteralExpression.LiteralType.LONG -> JKJavaPrimitiveTypeImpl.LONG
        JKLiteralExpression.LiteralType.FLOAT -> JKJavaPrimitiveTypeImpl.FLOAT
        JKLiteralExpression.LiteralType.DOUBLE -> JKJavaPrimitiveTypeImpl.DOUBLE
        JKLiteralExpression.LiteralType.STRING -> null
        JKLiteralExpression.LiteralType.NULL -> null
    }

fun JKJavaPrimitiveType.toLiteralType(): JKLiteralExpression.LiteralType? =
    when (this) {
        JKJavaPrimitiveTypeImpl.CHAR -> JKLiteralExpression.LiteralType.CHAR
        JKJavaPrimitiveTypeImpl.BOOLEAN -> JKLiteralExpression.LiteralType.BOOLEAN
        JKJavaPrimitiveTypeImpl.INT -> JKLiteralExpression.LiteralType.INT
        JKJavaPrimitiveTypeImpl.LONG -> JKLiteralExpression.LiteralType.LONG
        JKJavaPrimitiveTypeImpl.CHAR -> JKLiteralExpression.LiteralType.CHAR
        JKJavaPrimitiveTypeImpl.DOUBLE -> JKLiteralExpression.LiteralType.DOUBLE
        JKJavaPrimitiveTypeImpl.FLOAT -> JKLiteralExpression.LiteralType.FLOAT
        else -> null
    }


fun JKType.asPrimitiveType(): JKJavaPrimitiveType? =
    if (this is JKJavaPrimitiveType) this
    else when ((this as? JKClassType)?.classReference?.fqName) {
        KotlinBuiltIns.FQ_NAMES._char.asString(), CommonClassNames.JAVA_LANG_CHARACTER -> JKJavaPrimitiveTypeImpl.CHAR
        KotlinBuiltIns.FQ_NAMES._boolean.asString(), CommonClassNames.JAVA_LANG_BOOLEAN -> JKJavaPrimitiveTypeImpl.BOOLEAN
        KotlinBuiltIns.FQ_NAMES._int.asString(), CommonClassNames.JAVA_LANG_INTEGER -> JKJavaPrimitiveTypeImpl.INT
        KotlinBuiltIns.FQ_NAMES._long.asString(), CommonClassNames.JAVA_LANG_LONG -> JKJavaPrimitiveTypeImpl.LONG
        KotlinBuiltIns.FQ_NAMES._float.asString(), CommonClassNames.JAVA_LANG_FLOAT -> JKJavaPrimitiveTypeImpl.FLOAT
        KotlinBuiltIns.FQ_NAMES._double.asString(), CommonClassNames.JAVA_LANG_DOUBLE -> JKJavaPrimitiveTypeImpl.DOUBLE
        KotlinBuiltIns.FQ_NAMES._byte.asString(), CommonClassNames.JAVA_LANG_BYTE -> JKJavaPrimitiveTypeImpl.BYTE
        KotlinBuiltIns.FQ_NAMES._short.asString(), CommonClassNames.JAVA_LANG_SHORT -> JKJavaPrimitiveTypeImpl.SHORT
        else -> null
    }

fun JKJavaPrimitiveType.isNumberType() =
    this == JKJavaPrimitiveTypeImpl.INT ||
            this == JKJavaPrimitiveTypeImpl.LONG ||
            this == JKJavaPrimitiveTypeImpl.FLOAT ||
            this == JKJavaPrimitiveTypeImpl.DOUBLE

inline fun <reified T : JKType> T.addTypeParametersToRawProjectionType(typeParameter: JKType): T =
    if (this is JKClassType && parameters.isEmpty()) {
        val parametersCount = classReference.expectedTypeParametersCount()
        val typeParameters = List(parametersCount) { typeParameter }
        JKClassTypeImpl(
            classReference,
            typeParameters,
            nullability
        ) as T
    } else this

fun JKClassSymbol.expectedTypeParametersCount(): Int {
    val resolvedClass = target
    return when (resolvedClass) {
        is PsiClass -> resolvedClass.typeParameters.size
        is KtClass -> resolvedClass.typeParameters.size
        else -> 0
    }
}

val primitiveTypes =
    listOf(
        JvmPrimitiveType.BOOLEAN,
        JvmPrimitiveType.CHAR,
        JvmPrimitiveType.BYTE,
        JvmPrimitiveType.SHORT,
        JvmPrimitiveType.INT,
        JvmPrimitiveType.FLOAT,
        JvmPrimitiveType.LONG,
        JvmPrimitiveType.DOUBLE
    )

fun JKType.arrayFqName(): String =
    if (this is JKJavaPrimitiveType)
        PrimitiveType.valueOf(jvmPrimitiveType.name).arrayTypeFqName.asString()
    else KotlinBuiltIns.FQ_NAMES.array.asString()

fun JKClassSymbol.isArrayType(): Boolean =
    fqName in
            JKJavaPrimitiveTypeImpl.KEYWORD_TO_INSTANCE.values
                .filterIsInstance<JKJavaPrimitiveType>()
                .map { PrimitiveType.valueOf(it.jvmPrimitiveType.name).arrayTypeFqName.asString() } +
            KotlinBuiltIns.FQ_NAMES.array.asString()

fun JKType.isArrayType() =
    when (this) {
        is JKClassType -> classReference.isArrayType()
        is JKJavaArrayType -> true
        else -> false
    }


fun JKType.arrayInnerType(): JKType? =
    when (this) {
        is JKJavaArrayType -> type
        is JKClassType ->
            if (this.classReference.isArrayType()) this.parameters.singleOrNull()
            else null
        else -> null
    }

val namesOfPrimitiveTypes by lazy {
    KotlinBuiltIns.FQ_NAMES.primitiveTypeShortNames.map { it.identifier.decapitalize() }
}

fun JKClassSymbol.isInterface(): Boolean {
    val target = target
    return when (target) {
        is PsiClass -> target.isInterface
        is KtClass -> target.isInterface()
        is JKClass -> target.classKind == JKClass.ClassKind.INTERFACE
        else -> false
    }
}

fun JKType.isInterface(): Boolean =
    (this as? JKClassType)?.classReference?.isInterface() ?: false

fun JKMethod.nullabilityBySuperMethod(symbolProvider: JKSymbolProvider): Nullability {
    if (modality != Modality.OVERRIDE) return Nullability.Default
    val superMethodSymbol = findSuperMethodSymbol(symbolProvider) ?: return Nullability.Default
    return superMethodSymbol.returnType?.nullability ?: return Nullability.Default
}


private fun JKMethod.findSuperMethodSymbol(symbolProvider: JKSymbolProvider): JKMethodSymbol? =
    psi<PsiMethod>()
        ?.findSuperMethods()
        ?.firstOrNull()
        ?.let {
            symbolProvider.provideDirectSymbol(it) as? JKMethodSymbol
        }

fun JKType.replaceJavaClassWithKotlinClassType(symbolProvider: JKSymbolProvider): JKType =
    applyRecursive { type ->
        if (type is JKClassType && type.classReference.fqName == "java.lang.Class") {
            JKClassTypeImpl(
                symbolProvider.provideByFqName(KotlinBuiltIns.FQ_NAMES.kClass),
                type.parameters.map { it.replaceJavaClassWithKotlinClassType(symbolProvider) },
                Nullability.NotNull
            )
        } else null
    }
