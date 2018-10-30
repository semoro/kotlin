/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import org.jetbrains.kotlin.j2k.tree.JKKtAssignmentStatement
import org.jetbrains.kotlin.j2k.tree.JKTreeElement
import org.jetbrains.kotlin.j2k.tree.impl.*

class AssignmentStatementOperatorConversion : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        //todo fix
//        if (element !is JKKtAssignmentStatement) return recurse(element)
//        val token = element.operator.token as? JKJavaOperatorToken ?: return recurse(element)
//        val newToken = token.toKtToken()
//        if (newToken is JKKtWordOperatorToken) {
//            element.operator = JKKtOperatorImpl.tokenToOperator[KtTokens.EQ] ?: return recurse(element)
//            val expr = element.expression
//            element.expression = JKStubExpressionImpl()
//            element.expression = JKBinaryExpressionImpl(expr, element.field.copyTree(), newToken)
//        } else {
//            element.operator = newToken
//        }
        return recurse(element)
    }
}