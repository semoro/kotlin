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

package org.jetbrains.kotlin.j2k.tree

import org.jetbrains.kotlin.j2k.tree.visitors.JKTransformer
import org.jetbrains.kotlin.j2k.tree.visitors.JKVisitor


interface JKElement {
    fun <R, D> accept(visitor: JKVisitor<R, D>, data: D): R
    fun <R : JKElement, D> transform(transformer: JKTransformer<D>, data: D): R

    fun <D> acceptChildren(visitor: JKVisitor<Unit, D>, data: D)
    fun <D> transformChildren(transformer: JKTransformer<D>, data: D)
}

interface JKClass : JKDeclaration, JKModifierListOwner {
    var name: JKNameIdentifier
    var declarations: List<JKDeclaration>
    var classKind: ClassKind

    enum class ClassKind {
        ABSTRACT, ANNOTATION, CLASS, ENUM, INTERFACE
    }
}

interface JKStatement : JKElement

interface JKExpression : JKStatement

interface JKBinaryExpression : JKExpression {
    var left: JKExpression
    var right: JKExpression?
    var operator: JKOperatorIdentifier
}

interface JKUnaryExpression : JKExpression {
    val expression: JKExpression?
    var operator: JKOperatorIdentifier
}

interface JKPrefixExpression : JKUnaryExpression

interface JKPostfixExpression : JKUnaryExpression {
    override val expression: JKExpression
}

interface JKQualifiedExpression : JKExpression {
    var receiver: JKExpression
    var operator: JKQualificationIdentifier
    var selector: JKStatement
}

interface JKMethodCallExpression : JKExpression {
    var identifier: JKMethodReference
    var arguments: JKExpressionList
}

interface JKFieldAccessExpression : JKExpression {
    var identifier: JKFieldReference
}

interface JKArrayAccessExpression : JKExpression {
    var expression : JKExpression
    var indexExpression : JKExpression?
}

interface JKParenthesizedExpression : JKExpression {
    var expression : JKExpression?
}

interface JKTypeCastExpression : JKExpression {
    var expression : JKExpression?
    var type : JKTypeReference?
}

interface JKExpressionList : JKElement {
    var expressions: Array<JKExpression>
}

interface JKMethodReference : JKElement {

}

interface JKFieldReference : JKElement {

}

interface JKClassReference : JKElement {

}

interface JKTypeReference : JKElement {

}

interface JKOperatorIdentifier : JKIdentifier

interface JKQualificationIdentifier : JKIdentifier

interface JKLoop : JKStatement

interface JKDeclaration : JKElement

interface JKBlock : JKElement {
    var statements: List<JKStatement>
}

interface JKIdentifier : JKElement

interface JKTypeIdentifier : JKIdentifier

interface JKNameIdentifier : JKIdentifier {
    var name: String
}

interface JKLiteralExpression : JKExpression

interface JKModifierList : JKElement {
    var modifiers: List<JKModifier>
}

interface JKModifier : JKElement

interface JKAccessModifier : JKModifier

interface JKValueArgument : JKElement {
    var type: JKTypeIdentifier
    val name: String
}

interface JKStringLiteralExpression : JKLiteralExpression {
    val text: String
}

interface JKModalityModifier : JKModifier