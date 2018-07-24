/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.JKExpressionStatementImpl
import org.jetbrains.kotlin.j2k.tree.impl.JKStubExpressionImpl

class AssignmentStatementSimplifyAlsoConversion : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKExpressionStatement) return recurse(element)
        val qualifiedExpr = element.expression as? JKQualifiedExpression ?: return recurse(element)
        val alsoCall = qualifiedExpr.selector as? JKKtAlsoCallExpression ?: return recurse(element)
        val arg = qualifiedExpr.receiver.also { qualifiedExpr.receiver = JKStubExpressionImpl() }
        return recurse(if (alsoCall.statement !is JKBlockStatement) alsoCall.statement.also {
            alsoCall.statement = JKExpressionStatementImpl(JKStubExpressionImpl())
        }.also { inlineVal(it, arg) } else element)
    }

    private fun inlineVal(stat: JKStatement, expr: JKExpression) {
        if (stat is JKKtAssignmentStatement) {
            (stat.expression as? JKBinaryExpression)?.right = expr
            if (stat.expression !is JKBinaryExpression) stat.expression = expr
        }
    }
}