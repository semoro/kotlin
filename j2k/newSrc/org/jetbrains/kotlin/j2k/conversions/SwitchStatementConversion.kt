/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import com.intellij.psi.*
import com.intellij.psi.controlFlow.ControlFlowFactory
import com.intellij.psi.controlFlow.ControlFlowUtil
import com.intellij.psi.controlFlow.LocalsOrMyInstanceFieldsControlFlowPolicy
import org.jetbrains.kotlin.j2k.ConversionContext
import org.jetbrains.kotlin.j2k.copyTreeAndDetach
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.*


class SwitchStatementConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element is JKJavaSwitchStatementImpl) {
            element.invalidate()
            val cases = switchCasesToWhenCases(element.cases)
            val whenStatement = JKKtWhenStatementImpl(element.expression, cases)
            return recurse(whenStatement)
        }
        return recurse(element)
    }

    private fun switchCasesToWhenCases(cases: List<JKJavaSwitchCase>): List<JKKtWhenCase> {
        val result = mutableListOf<JKKtWhenCase>()
        var pendingSelectors = mutableListOf<JKKtWhenLabel>()
        var defaultSelector: JKKtWhenLabel? = null
        var defaultEntry: JKKtWhenCase? = null
        for ((i, case) in cases.withIndex()) {
            val label = createWhenLabel(case)
            if (case.isDefault()) defaultSelector = label else pendingSelectors.add(label)
            if (case.statements.isNotEmpty()) {
                val statement = convertCaseStatementsToBody(cases, i)
                if (pendingSelectors.isNotEmpty())
                    result.add(JKKtWhenCaseImpl(pendingSelectors, statement))
                if (defaultSelector != null)
                    defaultEntry = JKKtWhenCaseImpl(listOf(defaultSelector), statement)
                pendingSelectors = mutableListOf()
                defaultSelector = null
            }
        }
        defaultEntry?.let(result::add)
        return result
    }

    private fun createWhenLabel(switchCase: JKJavaSwitchCase): JKKtWhenLabel =
        when (switchCase) {
            is JKJavaLabelSwitchCase ->
                JKKtValueWhenLabelImpl(switchCase.label.also { it.detach(switchCase) })//TODO replace with detached
            else -> JKKtElseWhenLabelImpl()
        }

    private fun convertCaseStatements(statements: List<JKStatement>, allowBlock: Boolean = true): List<JKStatement> {
        val statementsToKeep = statements
            .filterNot(::isSwitchBreak)
            .map { it.copyTreeAndDetach() }

        if (allowBlock && statementsToKeep.size == 1) {
            val block = statementsToKeep.single() as? JKBlockStatement
            if (block != null) {
                block.block.statements = block.block.statements.filterNot(::isSwitchBreak)
                return listOf(block)
            }
        }
        return statementsToKeep
    }

    private fun convertCaseStatements(cases: List<JKJavaSwitchCase>, caseIndex: Int, allowBlock: Boolean = true): List<JKStatement> {
        val case = cases[caseIndex]
        val fallsThrough =
            if (caseIndex == cases.lastIndex) {
                false
            } else {
                val block = case.statements.singleOrNull() as? JKBlockStatement
                val statements = block?.block?.statements ?: case.statements
                statements.fallsThrough()
            }
        return if (fallsThrough) // we fall through into the next case
            convertCaseStatements(case.statements, allowBlock = false) + convertCaseStatements(cases, caseIndex + 1, allowBlock = false)
        else
            convertCaseStatements(case.statements, allowBlock)
    }

    private fun convertCaseStatementsToBody(cases: List<JKJavaSwitchCase>, caseIndex: Int): JKStatement {
        val statements = convertCaseStatements(cases, caseIndex)
        return if (statements.size == 1)
            statements.single()
        else
            JKBlockStatementImpl(JKBlockImpl(statements))
    }

    private fun isSwitchBreak(statement: JKStatement) = statement is JKBreakStatement && statement !is JKBreakWithLabelStatement

    private fun List<JKStatement>.fallsThrough(): Boolean {
        for (statement in this) {
            //TODO add support of this when will be added
            // is JKThrowStatement || is JKContinueStatement
            if (statement is JKBreakStatement || statement is JKReturnStatement) {
                return false
            }
            val psiStatement = context.backAnnotator(statement)
            when (psiStatement) {
                is PsiSwitchStatement -> if (!psiStatement.canCompleteNormally()) return false
                is PsiIfStatement -> if (!psiStatement.canCompleteNormally()) return false
            }
        }
        return true
    }

    private fun PsiElement.canCompleteNormally(): Boolean {
        val controlFlow =
            ControlFlowFactory.getInstance(project).getControlFlow(this, LocalsOrMyInstanceFieldsControlFlowPolicy.getInstance())
        val startOffset = controlFlow.getStartOffset(this)
        val endOffset = controlFlow.getEndOffset(this)
        return startOffset == -1 || endOffset == -1 || ControlFlowUtil.canCompleteNormally(controlFlow, startOffset, endOffset)
    }

}