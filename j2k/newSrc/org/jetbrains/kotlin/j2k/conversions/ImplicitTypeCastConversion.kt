/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import org.jetbrains.kotlin.j2k.ConversionContext
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType
import java.lang.Integer.max

class ImplicitTypeCastConversion(val context: ConversionContext) : RecursiveApplicableConversionBase() {

    val wideningOrder = LinkedHashSet<JKJavaPrimitiveType>()

    init {
        wideningOrder.add(JKJavaPrimitiveTypeImpl.BYTE)
        wideningOrder.add(JKJavaPrimitiveTypeImpl.SHORT)
        wideningOrder.add(JKJavaPrimitiveTypeImpl.INT)
        wideningOrder.add(JKJavaPrimitiveTypeImpl.LONG)
        wideningOrder.add(JKJavaPrimitiveTypeImpl.FLOAT)
        wideningOrder.add(JKJavaPrimitiveTypeImpl.DOUBLE)
    }


    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKJavaAssignmentExpression
            && element !is JKJavaMethodCallExpression
            && element !is JKBinaryExpression
        ) return recurse(element)
        if (element is JKJavaAssignmentExpression) {
            val expectedType = element.field.computeExpectedType()
            val actualType = element.expression.resolveType()
            val containsExpected = wideningOrder.contains(expectedType)
            val containsActual = wideningOrder.contains(actualType)
            if (containsActual && containsExpected) {
                val indexOfExpected = wideningOrder.indexOf(expectedType)
                var indexOfActual = wideningOrder.indexOf(actualType)
                lateinit var expectedPrimitive: JvmPrimitiveType
                lateinit var actualPrimitive: JKJavaPrimitiveType
                val field = element.field as JKFieldAccessExpression
                if (indexOfActual < indexOfExpected) {
                    val fqClassName = actualPrimitive.jvmPrimitiveType.wrapperFqName
                    val conversionMethodName = getConversionMethodName(expectedPrimitive) ?: return recurse(element)
                    val symbol = context.symbolProvider.provideDirectSymbol(
                        resolveFqName(
                            ClassId.topLevel(fqClassName).createNestedClassId(Name.identifier(conversionMethodName)),
                            element.getParentOfType<JKClass>() ?: element,
                            context
                        )!!
                    ) as JKMethodSymbol
                    return JKKtAssignmentStatenmentImpl(
                        JKKtFieldAccessExpressionImpl(field.identifier),
                        JKQualifiedExpressionImpl(
                            element.expression,
                            JKKtQualifierImpl.DOT,
                            JKKtCallExpressionImpl(symbol, JKExpressionListImpl(emptyList()))
                        ),
                        element.operator
                    )
                }
            }
        } else if (element is JKJavaMethodCallExpression) {
            val arguments = element.arguments
            element.identifier
        } else if (element is JKBinaryExpression) {

        }
        return recurse(element)
    }

    fun JKExpression.resolveType(): JKType? {
        when (this) {
            is JKLiteralExpression -> return toJKJavaPrimitiveType()
            is JKAssignableExpression -> return computeExpectedType()
            is JKArrayAccessExpression -> return computeExpectedType()
            is JKBinaryExpression -> return resolveType()
            else -> return null
        }
    }

    fun JKLiteralExpression.toJKJavaPrimitiveType(): JKJavaPrimitiveTypeImpl? {
        return when (type.primitiveType?.typeName?.identifier) {
            "Boolean" -> JKJavaPrimitiveTypeImpl.BOOLEAN
            "Char" -> JKJavaPrimitiveTypeImpl.CHAR
            "Byte" -> JKJavaPrimitiveTypeImpl.BYTE
            "Short" -> JKJavaPrimitiveTypeImpl.SHORT
            "Int" -> JKJavaPrimitiveTypeImpl.INT
            "Float" -> JKJavaPrimitiveTypeImpl.FLOAT
            "Long" -> JKJavaPrimitiveTypeImpl.LONG
            "Double" -> JKJavaPrimitiveTypeImpl.DOUBLE
            else -> null
        }
    }

    fun JKBinaryExpression.resolveType(): JKType? {
        val leftType = left.resolveType()
        val rightType = right.resolveType()
        if (leftType == rightType) return leftType
        if (leftType is JKJavaPrimitiveType && rightType is JKJavaPrimitiveType) {
            val indexOfLeft = wideningOrder.indexOf(leftType)
            val indexOfRight = wideningOrder.indexOf(rightType)
            if (indexOfLeft >= 0 && indexOfRight >= 0) return wideningOrder.elementAt(max(indexOfLeft, indexOfRight))
        }
        return null
    }
}

