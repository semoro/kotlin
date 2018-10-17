/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import jdk.nashorn.internal.ir.BlockStatement
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.*


class LabeledStatementConversion : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKLabeledStatement) return recurse(element)
        val statement = element.statement as? JKBlockStatement ?: return recurse(element)
        if (isConvertedForLoopToWhileOne(statement)) {
            val initStatement = statement.block.statements[0].detached()
            val whileStatement = statement.block.statements[1].detached()
            val whileStatementLabeled = JKLabeledStatementImpl(whileStatement, element.labels.also { it.forEach { it.detached() } })
            return recurse(JKBlockStatementImpl(JKBlockImpl(listOf(initStatement, whileStatementLabeled))))
        }
        return recurse(element)
    }

    private fun isConvertedForLoopToWhileOne(blockStatement: JKBlockStatement): Boolean =
        blockStatement.block.statements.size == 2 &&
                ((blockStatement.block.statements[0] as? JKDeclarationStatement)
                    ?.statements?.firstOrNull() as JKDeclarationStatementImpl)
                    .declaredStatements.firstOrNull() is JKLocalVariable &&
                blockStatement.block.statements[1] is JKWhileStatement
}