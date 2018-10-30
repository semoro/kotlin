/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.j2k.tree.impl

import com.intellij.psi.JavaTokenType
import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.visitors.JKVisitor
import org.jetbrains.kotlin.lexer.KtSingleValueToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.utils.addToStdlib.assertedCast
import org.jetbrains.kotlin.utils.addToStdlib.cast

class JKKtPropertyImpl(
    modifierList: JKModifierList,
    type: JKTypeElement,
    name: JKNameIdentifier,
    initializer: JKExpression,
    getter: JKBlock,
    setter: JKBlock
) : JKBranchElementBase(), JKKtProperty {
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtProperty(this, data)

    override var modifierList: JKModifierList by child(modifierList)
    override var type by child(type)
    override var name: JKNameIdentifier by child(name)
    override var initializer: JKExpression by child(initializer)
    override val getter: JKBlock by child(getter)
    override val setter: JKBlock by child(setter)
}

class JKKtFunctionImpl(
    returnType: JKTypeElement,
    name: JKNameIdentifier,
    parameters: List<JKParameter>,
    block: JKBlock,
    modifierList: JKModifierList
) : JKBranchElementBase(), JKKtFunction {
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtFunction(this, data)

    override var returnType: JKTypeElement by child(returnType)
    override var name: JKNameIdentifier by child(name)
    override var parameters: List<JKParameter> by children(parameters)
    override var block: JKBlock by child(block)
    override var modifierList: JKModifierList by child(modifierList)
}

sealed class JKKtQualifierImpl : JKQualifier, JKElementBase() {
    object DOT : JKKtQualifierImpl()
    object SAFE : JKKtQualifierImpl()
}

class JKKtCallExpressionImpl(
    override val identifier: JKMethodSymbol,
    arguments: JKExpressionList
) : JKKtMethodCallExpression, JKBranchElementBase() {
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtMethodCallExpression(this, data)

    override var arguments: JKExpressionList by child(arguments)
}

class JKKtFieldAccessExpressionImpl(override val identifier: JKFieldSymbol) : JKKtFieldAccessExpression, JKElementBase() {
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtFieldAccessExpression(this, data)
}

class JKKtLiteralExpressionImpl(
    override val literal: String,
    override val type: JKLiteralExpression.LiteralType
) : JKKtLiteralExpression,
    JKElementBase() {
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtLiteralExpression(this, data)
}

fun JKJavaOperatorImpl.toKtToken(): KtSingleValueToken =
    when (this.token) {
        JavaTokenType.DIV -> KtTokens.DIV
        JavaTokenType.MINUS -> KtTokens.MINUS
        JavaTokenType.ANDAND -> KtTokens.ANDAND
        JavaTokenType.OROR -> KtTokens.OROR
        JavaTokenType.PLUS -> KtTokens.PLUS
        JavaTokenType.ASTERISK -> KtTokens.MUL
        JavaTokenType.GT -> KtTokens.GT
        JavaTokenType.GE -> KtTokens.GTEQ
        JavaTokenType.LT -> KtTokens.LT
        JavaTokenType.LE -> KtTokens.LTEQ
        JavaTokenType.PERC -> KtTokens.PERC
        else -> TODO(this.token::class.java.toString())
    }

class JKKtOperatorImpl(val token: KtSingleValueToken, val methodSymbol: JKMethodSymbol) : JKOperator, JKElementBase() {
    override val precedence: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val operatorText: String
        get() = token.value
}

class JKKtWordOperatorImpl constructor(override val operatorText: String) : JKOperator, JKElementBase() {
    override val precedence: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}

class JKKtModifierImpl(override val type: JKKtModifier.KtModifierType) : JKKtModifier, JKElementBase() {
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtModifier(this, data)
}

class JKKtAlsoCallExpressionImpl(
    override val statement: JKStatement, override val identifier: JKMethodSymbol, override val parameterName: String = "it"
) : JKKtAlsoCallExpression, JKBranchElementBase() {
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtAlsoCallExpression(this, data)
    val parameter
        get() = arguments.expressions.first().cast<JKLambdaExpression>().parameters.first()
    override var arguments: JKExpressionList by child(
        JKExpressionListImpl(
            listOf(
                JKLambdaExpressionImpl(
                    listOf(
                        JKParameterImpl(
                            JKTypeElementImpl(JKJavaVoidType),
                            JKNameIdentifierImpl(parameterName),
                            JKModifierListImpl()
                        )
                    ), JKTypeElementImpl(JKJavaVoidType), statement
                )
            )
        )
    )
}

class JKKtAssignmentStatementImpl(
    override var field: JKAssignableExpression, expression: JKExpression, override var operator: JKOperator
) : JKKtAssignmentStatement, JKBranchElementBase() {
    override var expression by child(expression)
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtAssignmentStatement(this, data)

}

class JKKtConstructorImpl(
    name: JKNameIdentifier,
    parameters: List<JKParameter>,
    block: JKBlock,
    modifierList: JKModifierList,
    delegationCall: JKExpression
) : JKBranchElementBase(), JKKtConstructor {
    override val returnType: JKTypeElement get() = TODO("!")

    override var name: JKNameIdentifier by child(name)
    override var parameters: List<JKParameter> by children(parameters)
    override var block: JKBlock by child(block)
    override var modifierList: JKModifierList by child(modifierList)
    override var delegationCall: JKExpression by child(delegationCall)

    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtConstructor(this, data)
}

class JKKtPrimaryConstructorImpl(
    name: JKNameIdentifier,
    parameters: List<JKParameter>,
    block: JKBlock,
    modifierList: JKModifierList,
    delegationCall: JKExpression
) : JKBranchElementBase(), JKKtPrimaryConstructor {
    override val returnType: JKTypeElement get() = TODO("!")

    override var name: JKNameIdentifier by child(name)
    override var parameters: List<JKParameter> by children(parameters)
    override var block: JKBlock by child(block)
    override var modifierList: JKModifierList by child(modifierList)
    override var delegationCall: JKExpression by child(delegationCall)

    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtPrimaryConstructor(this, data)
}

class JKKtIsExpressionImpl(expression: JKExpression, type: JKTypeElement) : JKKtIsExpression, JKBranchElementBase() {
    override var type by child(type)
    override var expression by child(expression)

    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtIsExpression(this, data)
}

class JKKtInitDeclarationImpl(block: JKBlock) : JKKtInitDeclaration, JKBranchElementBase() {
    override var block: JKBlock by child(block)
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtInitDeclaration(this, data)
}

fun JKClass.getOrCreateInitDeclaration(): JKKtInitDeclaration {
    val existingDeclaration = declarationList.filterIsInstance<JKKtInitDeclaration>().firstOrNull()
    if (existingDeclaration != null) return existingDeclaration
    val newDeclaration = JKKtInitDeclarationImpl(JKBlockImpl())
    declarationList += newDeclaration
    return newDeclaration
}

class JKKtForInStatementImpl(variableIdentifier: JKNameIdentifier, iterationExpression: JKExpression, body: JKStatement) :
    JKKtForInStatement,
    JKBranchElementBase() {
    override var variableIdentifier: JKNameIdentifier by child(variableIdentifier)
    override var iterationExpression: JKExpression by child(iterationExpression)
    override var body: JKStatement by child(body)
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtForInStatement(this, data)
}

class JKKtOperatorExpressionImpl(
    receiver: JKExpression,
    override var identifier: JKMethodSymbol,
    argument: JKExpression
) : JKKtOperatorExpression, JKBranchElementBase() {
    override var receiver: JKExpression by child(receiver)
    override var argument: JKExpression by child(argument)
    override fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R = visitor.visitKtOperatorExpression(this, data)
}
