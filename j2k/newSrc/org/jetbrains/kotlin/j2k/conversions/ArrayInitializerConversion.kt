/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import org.jetbrains.kotlin.j2k.ConversionContext
import org.jetbrains.kotlin.j2k.ast.Nullability
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.*


class ArrayInitializerConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        var newElement = element
        if (element is JKJavaNewArray) {
            newElement = JKJavaMethodCallExpressionImpl(
                context.symbolProvider.provideByFqName(
                    when (element.type.type) {
                        JKJavaPrimitiveTypeImpl.BOOLEAN -> "kotlin/booleanArrayOf"
                        JKJavaPrimitiveTypeImpl.BYTE -> "kotlin/byteArrayOf"
                        JKJavaPrimitiveTypeImpl.CHAR -> "kotlin/charArrayOf"
                        JKJavaPrimitiveTypeImpl.DOUBLE -> "kotlin/doubleArrayOf"
                        JKJavaPrimitiveTypeImpl.FLOAT -> "kotlin/floatArrayOf"
                        JKJavaPrimitiveTypeImpl.INT -> "kotlin/intArrayOf"
                        JKJavaPrimitiveTypeImpl.LONG -> "kotlin/longArrayOf"
                        JKJavaPrimitiveTypeImpl.SHORT -> "kotlin/shortArrayOf"
                        else -> "kotlin/arrayOf"
                    }
                ),
                JKExpressionListImpl(element.initializer.also { element.initializer = emptyList() })
            )
        } else if (element is JKJavaNewEmptyArray) {
            newElement = buildArrayInitializer(element.initializer.also { element.initializer = emptyList() }, element.type.type)
        }

        return recurse(newElement)
    }

    private fun buildArrayInitializer(dimensions: List<JKExpression>, type: JKType): JKExpression {
        if (dimensions.size == 1) {
            val methodOrConstructorReference = if (type !is JKJavaPrimitiveType)
                context.symbolProvider.provideByFqName("kotlin/arrayOfNulls")
            else
                JKUnresolvedMethod(fqNameByType(type).replace('/', '.')/*TODO resolve real reference*/)
            return JKJavaMethodCallExpressionImpl(
                methodOrConstructorReference,
                JKExpressionListImpl(dimensions[0]),
                if (type is JKJavaPrimitiveType) emptyList() else listOf(JKTypeElementImpl(type))
            )
        }
        if (dimensions[1] !is JKStubExpression) {
            return JKJavaMethodCallExpressionImpl(
                JKUnresolvedMethod("kotlin.Array"),//TODO resolve real reference
                JKExpressionListImpl(
                    dimensions[0],
                    JKLambdaExpressionImpl(
                        statement = JKExpressionStatementImpl(buildArrayInitializer(dimensions.subList(1, dimensions.size), type))
                    )
                )
            )
        }
        var resultType = JKClassTypeImpl(
            context.symbolProvider.provideByFqName(fqNameByType(type)),
            if (type is JKJavaPrimitiveType) emptyList() else listOf(type),
            Nullability.NotNull
        )
        for (i in 0..dimensions.size - 3) {
            resultType = JKClassTypeImpl(context.symbolProvider.provideByFqName("kotlin/Array"), listOf(resultType), Nullability.NotNull)
        }
        return JKJavaMethodCallExpressionImpl(
            context.symbolProvider.provideByFqName("kotlin/arrayOfNulls"),
            JKExpressionListImpl(dimensions[0]),
            listOf(JKTypeElementImpl(resultType))
        )
    }

    private fun fqNameByType(type: JKType): String = when (type) {
        JKJavaPrimitiveTypeImpl.BOOLEAN -> "kotlin/BooleanArray"
        JKJavaPrimitiveTypeImpl.BYTE -> "kotlin/ByteArray"
        JKJavaPrimitiveTypeImpl.CHAR -> "kotlin/CharArray"
        JKJavaPrimitiveTypeImpl.DOUBLE -> "kotlin/DoubleArray"
        JKJavaPrimitiveTypeImpl.FLOAT -> "kotlin/FloatArray"
        JKJavaPrimitiveTypeImpl.INT -> "kotlin/IntArray"
        JKJavaPrimitiveTypeImpl.LONG -> "kotlin/LongArray"
        JKJavaPrimitiveTypeImpl.SHORT -> "kotlin/ShortArray"
        else -> "kotlin/Array"
    }
}