/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiVariable
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.j2k.*
import org.jetbrains.kotlin.j2k.ast.Nullability
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.JKClassSymbol
import org.jetbrains.kotlin.j2k.tree.impl.JKClassTypeImpl
import org.jetbrains.kotlin.j2k.tree.impl.JKJavaPrimitiveTypeImpl.*
import org.jetbrains.kotlin.j2k.tree.impl.JKJavaVoidType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtClassOrObject

class TypeMappingConversion(val context: ConversionContext) : RecursiveApplicableConversionBase() {
    private val unitSymbol = context.symbolProvider.provideByFqName<JKClassSymbol>(ClassId.topLevel(KotlinBuiltIns.FQ_NAMES.unit.toSafe()))

    private val typeFlavorCalculator = TypeFlavorCalculator(object : TypeFlavorConverterFacade {
        override val referenceSearcher: ReferenceSearcher
            get() = context.converter.converterServices.oldServices.referenceSearcher
        override val javaDataFlowAnalyzerFacade: JavaDataFlowAnalyzerFacade
            get() = context.converter.converterServices.oldServices.javaDataFlowAnalyzerFacade
        override val resolverForConverter: ResolverForConverter
            get() = context.converter.converterServices.oldServices.resolverForConverter

        override fun inConversionScope(element: PsiElement): Boolean = context.inConversionContext(element)

    })

    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element is JKTypeElement) {
            element.type = mapType(element.type)
        }
        return recurse(element)
    }

    private fun mapType(type: JKType): JKType = when (type) {
        is JKJavaPrimitiveType -> mapPrimitiveType(type)
        is JKClassType -> mapClassType(type).also { it.parameters = it.parameters.map(::mapType) }
        is JKJavaVoidType -> JKClassTypeImpl(unitSymbol, nullability = Nullability.NotNull)
        is JKJavaArrayType -> JKClassTypeImpl(
            context.symbolProvider.provideByFqName(fqNameByType(type.type)),
            if (type.type is JKJavaPrimitiveType) emptyList() else listOf(mapType(type.type)),
            type.nullability
        )
        else -> type
    }

    private fun mapClassType(type: JKClassType): JKClassType {
        val newFqName = JavaToKotlinClassMap.mapJavaToKotlin(FqName(type.classReference.fqName ?: return type)) ?: return type
        return JKClassTypeImpl(context.symbolProvider.provideByFqName(newFqName), type.parameters, calculateNullability(typeElement))
    }

    private fun mapPrimitiveType(type: JKJavaPrimitiveType): JKType {
        val fqName = type.jvmPrimitiveType.primitiveType.typeFqName
        return JKClassTypeImpl(context.symbolProvider.provideByFqName(ClassId.topLevel(fqName)), nullability = Nullability.NotNull)
    }

    private fun calculateNullability(typeElement: JKTypeElement): Nullability {
        val parent = typeElement.parent
        return when (parent) {
            is JKJavaMethod -> typeFlavorCalculator.methodNullability(context.backAnnotator(typeElement)!!.parent as PsiMethod)
            is JKJavaField -> typeFlavorCalculator.variableNullability(context.backAnnotator(typeElement)!!.parent as PsiVariable)
            is JKLocalVariable -> typeFlavorCalculator.variableNullability(context.backAnnotator(typeElement)!!.parent as PsiVariable)
            else -> Nullability.Default
        }
    }



    private fun fqNameByType(type: JKType): String = when (type) {
        BOOLEAN -> "kotlin/BooleanArray"
        BYTE -> "kotlin/ByteArray"
        CHAR -> "kotlin/CharArray"
        DOUBLE -> "kotlin/DoubleArray"
        FLOAT -> "kotlin/FloatArray"
        INT -> "kotlin/IntArray"
        LONG -> "kotlin/LongArray"
        SHORT -> "kotlin/ShortArray"
        else -> "kotlin/Array"
    }
}