/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import com.intellij.psi.*
import org.jetbrains.kotlin.j2k.ConversionContext
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.*
import org.jetbrains.kotlin.j2k.tree.impl.*
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.ClassId


class ForConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    private val referenceSearcher: ReferenceSearcher
        get() = context.converter.converterServices.oldServices.referenceSearcher

    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKJavaForLoopStatement) return recurse(element)

        convertToForeach(element)?.also { return it }
        return recurse(element)
    }

    private fun convertToForeach(loopStatement: JKJavaForLoopStatement): JKKtForInStatement? {
        val loopVar =
            (loopStatement.initializer as? JKDeclarationStatement)?.declaredStatements?.singleOrNull() as? JKLocalVariable ?: return null
        val loopVarPsi = loopVar.psi<PsiLocalVariable>() ?: return null
        val condition = loopStatement.condition as? JKBinaryExpression ?: return null
        if (!loopVarPsi.hasWriteAccesses(referenceSearcher, loopStatement.body.psi())
            && !loopVarPsi.hasWriteAccesses(referenceSearcher, loopStatement.condition.psi())
        ) {
            val left = condition.left as? JKFieldAccessExpression ?: return null
            val right = condition.right
            if (right.psi<PsiExpression>()?.type in listOf(PsiType.DOUBLE, PsiType.FLOAT, PsiType.CHAR)) return null
            if (left.identifier.target != loopVar) return null
            val start = loopVar.initializer
            val operationType = (loopStatement.updater as? JKExpressionStatement)?.expression?.isVariableIncrementOrDecrement(loopVar)
            val reversed = when ((operationType  as? JKJavaOperatorImpl)?.token) {
                JavaTokenType.PLUSPLUS -> false
                JavaTokenType.MINUSMINUS -> true
                else -> return null
            }
            val inclusive = when ((condition.operator as? JKJavaOperatorImpl)?.token ?: (condition.operator as? JKKtOperatorImpl)?.token) {
                JavaTokenType.LT, KtTokens.LT -> if (reversed) return null else false
                JavaTokenType.LE, KtTokens.LTEQ -> if (reversed) return null else true
                JavaTokenType.GT, KtTokens.GT -> if (reversed) false else return null
                JavaTokenType.GE, KtTokens.GTEQ -> if (reversed) true else return null
                JavaTokenType.NE, KtTokens.EXCLEQ -> false
                else -> return null
            }
            val range = forIterationRange(start, right, reversed, inclusive)
            //TODO
//            val explicitType = if (context.converter.settings.specifyLocalVariableTypeByDefault)
//                JKJavaPrimitiveTypeImpl.INT
//            else null
            (loopVar as? JKBranchElement)?.invalidate()
            (loopStatement as? JKBranchElement)?.invalidate()
            range.detach(range.parent!!)
            return JKKtForInStatementImpl(
                loopVar.name,
                range,
                loopStatement.body
            )

        }
        return null
    }

    private fun forIterationRange(start: JKExpression, bound: JKExpression, reversed: Boolean, inclusiveComparison: Boolean): JKExpression {
        indicesIterationRange(start, bound, reversed, inclusiveComparison)?.also { return it }

        TODO()
    }

    private fun indicesIterationRange(
        start: JKExpression,
        bound: JKExpression,
        reversed: Boolean,
        inclusiveComparison: Boolean
    ): JKExpression? {
        val collectionSizeExpression =
            if (reversed) {
                if (!inclusiveComparison) return null

                if ((bound as? JKLiteralExpression)?.literal?.toIntOrNull() != 0) return null

                if (start !is JKBinaryExpression) return null
                if ((start.operator as? JKJavaOperatorImpl)?.token != JavaTokenType.MINUS) return null
                if ((start.right as? JKLiteralExpression)?.literal?.toIntOrNull() != 1) return null
                start.left
            } else {
                if (inclusiveComparison) return null
                if ((start as? JKLiteralExpression)?.literal?.toIntOrNull() != 0) return null
                bound
            } as? JKQualifiedExpression ?: return null

        val indices = indeciesByCollectionSize(collectionSizeExpression) ?: return null

        val psiContext = collectionSizeExpression.psi<PsiExpression>() ?: return null
        return if (reversed) {
            val reversedSymbol = context.symbolProvider.provideDirectSymbol(
                multiResolveFqName(ClassId.fromString("kotlin/collections/reversed"), psiContext).first()
            ) as JKMethodSymbol
            JKQualifiedExpressionImpl(
                indices,
                JKKtQualifierImpl.DOT,
                JKJavaMethodCallExpressionImpl(reversedSymbol, JKExpressionListImpl())
            )
        } else indices
    }


    private fun indeciesByCollectionSize(javaSizeCall: JKQualifiedExpression): JKQualifiedExpression? {
        val methodCall = javaSizeCall.selector as? JKMethodCallExpression ?: return null
        //TODO check if receiver type is Collection
        if (methodCall.identifier.name == "size" && methodCall.arguments.expressions.isEmpty()) {
            val psiContext = javaSizeCall.psi<PsiExpression>() ?: return null
            val indiciesSymbol = context.symbolProvider.provideDirectSymbol(
                multiResolveFqName(ClassId.fromString("kotlin/collections/indices"), psiContext).first()
            ) as JKMultiversePropertySymbol
            javaSizeCall.selector = JKKtFieldAccessExpressionImpl(indiciesSymbol)
            return javaSizeCall
        }
        return null
    }


    private fun JKExpression.isVariableIncrementOrDecrement(variable: JKLocalVariable): JKOperator? {
        val pair = when (this) {
            is JKPostfixExpression -> operator to expression
            is JKPrefixExpression -> operator to expression
            else -> return null
        }
        if ((pair.second as? JKFieldAccessExpression)?.identifier?.target != variable) return null
        return pair.first
    }

    private inline fun <reified ElementType : PsiElement> JKElement.psi() =
        context.backAnnotator(this) as? ElementType
}