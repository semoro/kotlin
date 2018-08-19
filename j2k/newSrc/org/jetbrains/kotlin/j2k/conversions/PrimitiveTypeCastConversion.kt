/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import org.jetbrains.kotlin.j2k.ConversionContext
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.JvmPrimitiveType

class PrimitiveTypeCastConversion(val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKTypeCastExpression) return recurse(element)
        val expression = element.expression
        val type = element.type.type as? JKJavaPrimitiveType ?: return recurse(element)
        if (expression is JKJavaLiteralExpressionImpl) {
            val fqClassName = computeReturnTypeFqName(expression) ?: return recurse(element)
            val conversionMethodName = getConversionMethodName(type.jvmPrimitiveType) ?: return recurse(element)
            val symbol = context.symbolProvider.provideDirectSymbol(
                resolveFqName(
                    ClassId.topLevel(fqClassName).createNestedClassId(Name.identifier(conversionMethodName)),
                    element.getParentOfType<JKClass>() ?: element,
                    context
                )!!
            ) as JKMethodSymbol
            return JKQualifiedExpressionImpl(
                expression,
                JKKtQualifierImpl.DOT,
                JKKtCallExpressionImpl(symbol, JKExpressionListImpl(emptyList()))
            )
        } else {
            // TODO implement for non literal expressions
        }
        return recurse(element)
    }
}

fun computeReturnTypeFqName(expression: JKExpression): FqName? {
    // TODO implement computing type of any expression
    when (expression) {
        is JKLiteralExpression -> {
            val primitiveType = expression.type.primitiveType
            if (primitiveType != null) {
                return primitiveType.typeFqName
            }
        }
        is JKAssignableExpression -> {
            val expectedType = expression.computeExpectedType()
            if (expectedType is JKJavaPrimitiveType)
                return expectedType.jvmPrimitiveType.wrapperFqName
        }
    }
    return null
}

fun getConversionMethodName(jvmPrimitiveType: JvmPrimitiveType): String? {
    return when (jvmPrimitiveType) {
        JvmPrimitiveType.BYTE -> "toByte"
        JvmPrimitiveType.CHAR -> "toChar"
        JvmPrimitiveType.SHORT -> "toShort"
        JvmPrimitiveType.INT -> "toInt"
        JvmPrimitiveType.FLOAT -> "toFloat"
        JvmPrimitiveType.LONG -> "toLong"
        JvmPrimitiveType.DOUBLE -> "toDouble"
        else -> null
    }
}