/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.nj2k.conversions

import org.jetbrains.kotlin.nj2k.*
import org.jetbrains.kotlin.nj2k.tree.*
import org.jetbrains.kotlin.nj2k.tree.impl.*


class SynchronizedStatementConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKJavaSynchronizedStatement) return recurse(element)
        element.invalidate()
        val lambdaBody = JKLambdaExpressionImpl(
            JKBlockStatementImpl(element.body),
            emptyList()
        )
        val synchronizedCall =
            JKKtCallExpressionImpl(
                context.symbolProvider.provideByFqNameMulti("kotlin.synchronized"),
                JKExpressionListImpl(
                    element.lockExpression,
                    lambdaBody
                )
            )
        return recurse(JKExpressionStatementImpl(synchronizedCall))
    }

}