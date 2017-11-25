package org.jetbrains.kotlin.j2k.tree.visitors

import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.JKTypeReferenceImpl

interface JKVisitor<out R, in D> {
    fun visitElement(element: JKElement, data: D): R 
    fun visitClass(klass: JKClass, data: D): R = visitDeclaration(klass, data)
    fun visitStatement(statement: JKStatement, data: D): R = visitElement(statement, data)
    fun visitExpression(expression: JKExpression, data: D): R = visitStatement(expression, data)
    fun visitBinaryExpression(binaryExpression: JKBinaryExpression, data: D): R = visitExpression(binaryExpression, data)
    fun visitUnaryExpression(unaryExpression: JKUnaryExpression, data: D): R = visitExpression(unaryExpression, data)
    fun visitPrefixExpression(prefixExpression: JKPrefixExpression, data: D): R = visitUnaryExpression(prefixExpression, data)
    fun visitPostfixExpression(postfixExpression: JKPostfixExpression, data: D): R = visitUnaryExpression(postfixExpression, data)
    fun visitQualifiedExpression(qualifiedExpression: JKQualifiedExpression, data: D): R = visitExpression(qualifiedExpression, data)
    fun visitMethodCallExpression(methodCallExpression: JKMethodCallExpression, data: D): R = visitExpression(methodCallExpression, data)
    fun visitFieldAccessExpression(fieldAccessExpression: JKFieldAccessExpression, data: D): R = visitExpression(fieldAccessExpression, data)
    fun visitArrayAccessExpression(arrayAccessExpression: JKArrayAccessExpression, data: D): R = visitExpression(arrayAccessExpression, data)
    fun visitParenthesizedExpression(parenthesizedExpression: JKParenthesizedExpression, data: D): R = visitExpression(parenthesizedExpression, data)
    fun visitTypeCastExpression(typeCastExpression: JKTypeCastExpression, data: D): R = visitExpression(typeCastExpression, data)
    fun visitExpressionList(expressionList: JKExpressionList, data: D): R = visitElement(expressionList, data)
    fun visitMethodReference(methodReference: JKMethodReference, data: D): R = visitElement(methodReference, data)
    fun visitFieldReference(fieldReference: JKFieldReference, data: D): R = visitElement(fieldReference, data)
    fun visitClassReference(classReference: JKClassReference, data: D): R = visitElement(classReference, data)
    fun visitTypeReference(typeReference: JKTypeReferenceImpl, data: D): R = visitElement(typeReference, data)
    fun visitOperatorIdentifier(operatorIdentifier: JKOperatorIdentifier, data: D): R = visitIdentifier(operatorIdentifier, data)
    fun visitQualificationIdentifier(qualificationIdentifier: JKQualificationIdentifier, data: D): R = visitIdentifier(qualificationIdentifier, data)
    fun visitLoop(loop: JKLoop, data: D): R = visitStatement(loop, data)
    fun visitDeclaration(declaration: JKDeclaration, data: D): R = visitElement(declaration, data)
    fun visitBlock(block: JKBlock, data: D): R = visitElement(block, data)
    fun visitIdentifier(identifier: JKIdentifier, data: D): R = visitElement(identifier, data)
    fun visitTypeIdentifier(typeIdentifier: JKTypeIdentifier, data: D): R = visitIdentifier(typeIdentifier, data)
    fun visitNameIdentifier(nameIdentifier: JKNameIdentifier, data: D): R = visitIdentifier(nameIdentifier, data)
    fun visitLiteralExpression(literalExpression: JKLiteralExpression, data: D): R = visitExpression(literalExpression, data)
    fun visitModifierList(modifierList: JKModifierList, data: D): R = visitElement(modifierList, data)
    fun visitModifier(modifier: JKModifier, data: D): R = visitElement(modifier, data)
    fun visitAccessModifier(accessModifier: JKAccessModifier, data: D): R = visitModifier(accessModifier, data)
    fun visitValueArgument(valueArgument: JKValueArgument, data: D): R = visitElement(valueArgument, data)
    fun visitStringLiteralExpression(stringLiteralExpression: JKStringLiteralExpression, data: D): R = visitLiteralExpression(stringLiteralExpression, data)
    fun visitModalityModifier(modalityModifier: JKModalityModifier, data: D): R = visitModifier(modalityModifier, data)
    fun visitAnnotationUse(annotationUse: JKAnnotationUse, data: D): R = visitMethodCallExpression(annotationUse, data)
    fun visitJavaField(javaField: JKJavaField, data: D): R = visitDeclaration(javaField, data)
    fun visitJavaMethod(javaMethod: JKJavaMethod, data: D): R = visitDeclaration(javaMethod, data)
    fun visitJavaForLoop(javaForLoop: JKJavaForLoop, data: D): R = visitLoop(javaForLoop, data)
    fun visitJavaAssignmentExpression(javaAssignmentExpression: JKJavaAssignmentExpression, data: D): R = visitExpression(javaAssignmentExpression, data)
    fun visitJavaTypeIdentifier(javaTypeIdentifier: JKJavaTypeIdentifier, data: D): R = visitTypeIdentifier(javaTypeIdentifier, data)
    fun visitJavaStringLiteralExpression(javaStringLiteralExpression: JKJavaStringLiteralExpression, data: D): R = visitLiteralExpression(javaStringLiteralExpression, data)
    fun visitJavaOperatorIdentifier(javaOperatorIdentifier: JKJavaOperatorIdentifier, data: D): R = visitOperatorIdentifier(javaOperatorIdentifier, data)
    fun visitJavaQualificationIdentifier(javaQualificationIdentifier: JKJavaQualificationIdentifier, data: D): R = visitQualificationIdentifier(javaQualificationIdentifier, data)
    fun visitJavaMethodCallExpression(javaMethodCallExpression: JKJavaMethodCallExpression, data: D): R = visitMethodCallExpression(javaMethodCallExpression, data)
    fun visitJavaFieldAccessExpression(javaFieldAccessExpression: JKJavaFieldAccessExpression, data: D): R = visitFieldAccessExpression(javaFieldAccessExpression, data)
    fun visitJavaNewExpression(javaNewExpression: JKJavaNewExpression, data: D): R = visitExpression(javaNewExpression, data)
    fun visitJavaMethodReference(javaMethodReference: JKJavaMethodReference, data: D): R = visitMethodReference(javaMethodReference, data)
    fun visitJavaFieldReference(javaFieldReference: JKJavaFieldReference, data: D): R = visitFieldReference(javaFieldReference, data)
    fun visitJavaClassReference(javaClassReference: JKJavaClassReference, data: D): R = visitClassReference(javaClassReference, data)
    fun visitJavaAccessModifier(javaAccessModifier: JKJavaAccessModifier, data: D): R = visitAccessModifier(javaAccessModifier, data)
    fun visitJavaModifier(javaModifier: JKJavaModifier, data: D): R = visitModifier(javaModifier, data)
    fun visitJavaNewEmptyArray(javaNewEmptyArray: JKJavaNewEmptyArray, data: D): R = visitExpression(javaNewEmptyArray, data)
    fun visitJavaNewArray(javaNewArray: JKJavaNewArray, data: D): R = visitExpression(javaNewArray, data)
    fun visitKtFun(ktFun: JKKtFun, data: D): R = visitDeclaration(ktFun, data)
    fun visitKtConstructor(ktConstructor: JKKtConstructor, data: D): R = visitDeclaration(ktConstructor, data)
    fun visitKtPrimaryConstructor(ktPrimaryConstructor: JKKtPrimaryConstructor, data: D): R = visitKtConstructor(ktPrimaryConstructor, data)
    fun visitKtAssignmentStatement(ktAssignmentStatement: JKKtAssignmentStatement, data: D): R = visitStatement(ktAssignmentStatement, data)
    fun visitKtCall(ktCall: JKKtCall, data: D): R = visitMethodCallExpression(ktCall, data)
    fun visitKtProperty(ktProperty: JKKtProperty, data: D): R = visitDeclaration(ktProperty, data)
}
