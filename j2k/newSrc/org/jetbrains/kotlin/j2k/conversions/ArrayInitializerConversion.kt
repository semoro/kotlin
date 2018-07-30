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
        return when {
            dimensions.size == 1 -> JKJavaMethodCallExpressionImpl(
                if (type !is JKJavaPrimitiveType) context.symbolProvider.provideByFqName("kotlin/arrayOfNulls") else JKUnresolvedMethod(
                    fqNameByType(type).replace('/', '.')//TODO
                ),
                JKExpressionListImpl(dimensions[0]),
                if (type is JKJavaPrimitiveType) emptyList() else listOf(JKTypeElementImpl(type))
            )
            dimensions[1] !is JKStubExpression -> JKJavaMethodCallExpressionImpl(
                JKUnresolvedMethod("kotlin.Array"),//TODO
                JKExpressionListImpl(
                    dimensions[0],
                    JKLambdaExpressionImpl(
                        statement = JKExpressionStatementImpl(buildArrayInitializer(dimensions.subList(1, dimensions.size), type))
                    )
                )
            )
            else -> JKJavaMethodCallExpressionImpl(
                context.symbolProvider.provideByFqName("kotlin/arrayOfNulls"),
                JKExpressionListImpl(dimensions[0]),
                listOf(
                    JKTypeElementImpl(
                        Array(dimensions.size - 2) {
                            JKClassTypeImpl(context.symbolProvider.provideByFqName("kotlin/Array"), nullability = Nullability.NotNull)
                        }.fold(
                            JKClassTypeImpl(
                                context.symbolProvider.provideByFqName(fqNameByType(type)),
                                if (type is JKJavaPrimitiveType) emptyList() else listOf(type),
                                Nullability.NotNull
                            )
                        ) { a, b -> b.also { it.parameters = listOf(a) } })
                )
            )
        }
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