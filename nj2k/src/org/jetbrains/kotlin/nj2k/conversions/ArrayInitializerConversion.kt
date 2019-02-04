/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.nj2k.conversions

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.nj2k.ConversionContext
import org.jetbrains.kotlin.j2k.ast.Nullability
import org.jetbrains.kotlin.nj2k.tree.*
import org.jetbrains.kotlin.nj2k.tree.impl.*
import org.jetbrains.kotlin.resolve.CollectionLiteralResolver


class ArrayInitializerConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        var newElement = element
        if (element is JKJavaNewArray) {
            val arrayType = element.type.type as? JKJavaPrimitiveType
            val arrayConstructorName =
                if (arrayType != null)
                    CollectionLiteralResolver.PRIMITIVE_TYPE_TO_ARRAY[PrimitiveType.valueOf(arrayType.jvmPrimitiveType.name)]!!.asString()
                else
                    CollectionLiteralResolver.ARRAY_OF_FUNCTION.asString()
            newElement = JKJavaMethodCallExpressionImpl(
                context.symbolProvider.provideByFqName("kotlin.$arrayConstructorName"),
                JKExpressionListImpl(element.initializer.also { element.initializer = emptyList() })
            )
        } else if (element is JKJavaNewEmptyArray) {
            newElement = buildArrayInitializer(
                element.initializer.also { element.initializer = emptyList() }, element.type.type
            )
        }

        return recurse(newElement)
    }

    private fun buildArrayInitializer(dimensions: List<JKExpression>, type: JKType): JKExpression {
        if (dimensions.size == 1) {
            return if (type !is JKJavaPrimitiveType) {
                JKJavaMethodCallExpressionImpl(
                    context.symbolProvider.provideByFqName("kotlin/arrayOfNulls"),
                    JKExpressionListImpl(dimensions[0]),
                    JKTypeArgumentListImpl(listOf(JKTypeElementImpl(type.updateNullability(Nullability.NotNull))))
                )
            } else {
                JKJavaNewExpressionImpl(
                    context.symbolProvider.provideByFqName(type.arrayFqName()),
                    JKExpressionListImpl(dimensions[0]),
                    JKTypeArgumentListImpl(emptyList())
                )
            }
        }
        if (dimensions[1] !is JKStubExpression) {
            return JKJavaMethodCallExpressionImpl(
                context.symbolProvider.provideByFqName("kotlin.Array"),
                JKExpressionListImpl(
                    dimensions[0],
                    JKLambdaExpressionImpl(
                        JKExpressionStatementImpl(buildArrayInitializer(dimensions.subList(1, dimensions.size), type)),
                        emptyList()
                    )
                )
            )
        }
        var resultType = JKClassTypeImpl(
            context.symbolProvider.provideByFqName(type.arrayFqName()),
            if (type is JKJavaPrimitiveType) emptyList() else listOf(type),
            Nullability.Default
        )
        for (i in 0 until dimensions.size - 2) {
            resultType = JKClassTypeImpl(
                context.symbolProvider.provideByFqName(KotlinBuiltIns.FQ_NAMES.array.asString()),
                listOf(resultType),
                Nullability.NotNull
            )
        }
        return JKJavaMethodCallExpressionImpl(
            context.symbolProvider.provideByFqName("kotlin/arrayOfNulls"),
            JKExpressionListImpl(dimensions[0]),
            JKTypeArgumentListImpl(listOf(JKTypeElementImpl(resultType)))
        )
    }
}