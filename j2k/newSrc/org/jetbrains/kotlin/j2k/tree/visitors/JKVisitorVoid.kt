package org.jetbrains.kotlin.j2k.tree.visitors

import org.jetbrains.kotlin.j2k.tree.*

interface JKVisitorVoid : JKVisitor<Unit, Nothing?> {
    fun visitTreeElement(treeElement: JKTreeElement) 
    override fun visitTreeElement(treeElement: JKTreeElement, data: Nothing?) = visitTreeElement(treeElement)
    fun visitDeclaration(declaration: JKDeclaration) = visitTreeElement(declaration, null)
    override fun visitDeclaration(declaration: JKDeclaration, data: Nothing?) = visitDeclaration(declaration)
    fun visitFile(file: JKFile) = visitTreeElement(file, null)
    override fun visitFile(file: JKFile, data: Nothing?) = visitFile(file)
    fun visitClass(klass: JKClass) = visitDeclaration(klass, null)
    override fun visitClass(klass: JKClass, data: Nothing?) = visitClass(klass)
    fun visitInheritanceInfo(inheritanceInfo: JKInheritanceInfo) = visitTreeElement(inheritanceInfo, null)
    override fun visitInheritanceInfo(inheritanceInfo: JKInheritanceInfo, data: Nothing?) = visitInheritanceInfo(inheritanceInfo)
    fun visitMethod(method: JKMethod) = visitDeclaration(method, null)
    override fun visitMethod(method: JKMethod, data: Nothing?) = visitMethod(method)
    fun visitField(field: JKField) = visitDeclaration(field, null)
    override fun visitField(field: JKField, data: Nothing?) = visitField(field)
    fun visitLocalVariable(localVariable: JKLocalVariable) = visitField(localVariable, null)
    override fun visitLocalVariable(localVariable: JKLocalVariable, data: Nothing?) = visitLocalVariable(localVariable)
    fun visitModifier(modifier: JKModifier) = visitTreeElement(modifier, null)
    override fun visitModifier(modifier: JKModifier, data: Nothing?) = visitModifier(modifier)
    fun visitModifierList(modifierList: JKModifierList) = visitTreeElement(modifierList, null)
    override fun visitModifierList(modifierList: JKModifierList, data: Nothing?) = visitModifierList(modifierList)
    fun visitAccessModifier(accessModifier: JKAccessModifier) = visitModifier(accessModifier, null)
    override fun visitAccessModifier(accessModifier: JKAccessModifier, data: Nothing?) = visitAccessModifier(accessModifier)
    fun visitModalityModifier(modalityModifier: JKModalityModifier) = visitModifier(modalityModifier, null)
    override fun visitModalityModifier(modalityModifier: JKModalityModifier, data: Nothing?) = visitModalityModifier(modalityModifier)
    fun visitMutabilityModifier(mutabilityModifier: JKMutabilityModifier) = visitModifier(mutabilityModifier, null)
    override fun visitMutabilityModifier(mutabilityModifier: JKMutabilityModifier, data: Nothing?) = visitMutabilityModifier(mutabilityModifier)
    fun visitTypeElement(typeElement: JKTypeElement) = visitTreeElement(typeElement, null)
    override fun visitTypeElement(typeElement: JKTypeElement, data: Nothing?) = visitTypeElement(typeElement)
    fun visitStatement(statement: JKStatement) = visitTreeElement(statement, null)
    override fun visitStatement(statement: JKStatement, data: Nothing?) = visitStatement(statement)
    fun visitBlock(block: JKBlock) = visitTreeElement(block, null)
    override fun visitBlock(block: JKBlock, data: Nothing?) = visitBlock(block)
    fun visitIdentifier(identifier: JKIdentifier) = visitTreeElement(identifier, null)
    override fun visitIdentifier(identifier: JKIdentifier, data: Nothing?) = visitIdentifier(identifier)
    fun visitNameIdentifier(nameIdentifier: JKNameIdentifier) = visitIdentifier(nameIdentifier, null)
    override fun visitNameIdentifier(nameIdentifier: JKNameIdentifier, data: Nothing?) = visitNameIdentifier(nameIdentifier)
    fun visitExpression(expression: JKExpression) = visitTreeElement(expression, null)
    override fun visitExpression(expression: JKExpression, data: Nothing?) = visitExpression(expression)
    fun visitExpressionStatement(expressionStatement: JKExpressionStatement) = visitStatement(expressionStatement, null)
    override fun visitExpressionStatement(expressionStatement: JKExpressionStatement, data: Nothing?) = visitExpressionStatement(expressionStatement)
    fun visitDeclarationStatement(declarationStatement: JKDeclarationStatement) = visitStatement(declarationStatement, null)
    override fun visitDeclarationStatement(declarationStatement: JKDeclarationStatement, data: Nothing?) = visitDeclarationStatement(declarationStatement)
    fun visitBinaryExpression(binaryExpression: JKBinaryExpression) = visitExpression(binaryExpression, null)
    override fun visitBinaryExpression(binaryExpression: JKBinaryExpression, data: Nothing?) = visitBinaryExpression(binaryExpression)
    fun visitUnaryExpression(unaryExpression: JKUnaryExpression) = visitExpression(unaryExpression, null)
    override fun visitUnaryExpression(unaryExpression: JKUnaryExpression, data: Nothing?) = visitUnaryExpression(unaryExpression)
    fun visitPrefixExpression(prefixExpression: JKPrefixExpression) = visitUnaryExpression(prefixExpression, null)
    override fun visitPrefixExpression(prefixExpression: JKPrefixExpression, data: Nothing?) = visitPrefixExpression(prefixExpression)
    fun visitPostfixExpression(postfixExpression: JKPostfixExpression) = visitUnaryExpression(postfixExpression, null)
    override fun visitPostfixExpression(postfixExpression: JKPostfixExpression, data: Nothing?) = visitPostfixExpression(postfixExpression)
    fun visitQualifiedExpression(qualifiedExpression: JKQualifiedExpression) = visitExpression(qualifiedExpression, null)
    override fun visitQualifiedExpression(qualifiedExpression: JKQualifiedExpression, data: Nothing?) = visitQualifiedExpression(qualifiedExpression)
    fun visitMethodCallExpression(methodCallExpression: JKMethodCallExpression) = visitExpression(methodCallExpression, null)
    override fun visitMethodCallExpression(methodCallExpression: JKMethodCallExpression, data: Nothing?) = visitMethodCallExpression(methodCallExpression)
    fun visitFieldAccessExpression(fieldAccessExpression: JKFieldAccessExpression) = visitAssignableExpression(fieldAccessExpression, null)
    override fun visitFieldAccessExpression(fieldAccessExpression: JKFieldAccessExpression, data: Nothing?) = visitFieldAccessExpression(fieldAccessExpression)
    fun visitClassAccessExpression(classAccessExpression: JKClassAccessExpression) = visitExpression(classAccessExpression, null)
    override fun visitClassAccessExpression(classAccessExpression: JKClassAccessExpression, data: Nothing?) = visitClassAccessExpression(classAccessExpression)
    fun visitArrayAccessExpression(arrayAccessExpression: JKArrayAccessExpression) = visitAssignableExpression(arrayAccessExpression, null)
    override fun visitArrayAccessExpression(arrayAccessExpression: JKArrayAccessExpression, data: Nothing?) = visitArrayAccessExpression(arrayAccessExpression)
    fun visitParenthesizedExpression(parenthesizedExpression: JKParenthesizedExpression) = visitExpression(parenthesizedExpression, null)
    override fun visitParenthesizedExpression(parenthesizedExpression: JKParenthesizedExpression, data: Nothing?) = visitParenthesizedExpression(parenthesizedExpression)
    fun visitTypeCastExpression(typeCastExpression: JKTypeCastExpression) = visitExpression(typeCastExpression, null)
    override fun visitTypeCastExpression(typeCastExpression: JKTypeCastExpression, data: Nothing?) = visitTypeCastExpression(typeCastExpression)
    fun visitExpressionList(expressionList: JKExpressionList) = visitTreeElement(expressionList, null)
    override fun visitExpressionList(expressionList: JKExpressionList, data: Nothing?) = visitExpressionList(expressionList)
    fun visitLiteralExpression(literalExpression: JKLiteralExpression) = visitExpression(literalExpression, null)
    override fun visitLiteralExpression(literalExpression: JKLiteralExpression, data: Nothing?) = visitLiteralExpression(literalExpression)
    fun visitParameter(parameter: JKParameter) = visitField(parameter, null)
    override fun visitParameter(parameter: JKParameter, data: Nothing?) = visitParameter(parameter)
    fun visitStringLiteralExpression(stringLiteralExpression: JKStringLiteralExpression) = visitLiteralExpression(stringLiteralExpression, null)
    override fun visitStringLiteralExpression(stringLiteralExpression: JKStringLiteralExpression, data: Nothing?) = visitStringLiteralExpression(stringLiteralExpression)
    fun visitStubExpression(stubExpression: JKStubExpression) = visitExpression(stubExpression, null)
    override fun visitStubExpression(stubExpression: JKStubExpression, data: Nothing?) = visitStubExpression(stubExpression)
    fun visitLoopStatement(loopStatement: JKLoopStatement) = visitStatement(loopStatement, null)
    override fun visitLoopStatement(loopStatement: JKLoopStatement, data: Nothing?) = visitLoopStatement(loopStatement)
    fun visitBlockStatement(blockStatement: JKBlockStatement) = visitStatement(blockStatement, null)
    override fun visitBlockStatement(blockStatement: JKBlockStatement, data: Nothing?) = visitBlockStatement(blockStatement)
    fun visitThisExpression(thisExpression: JKThisExpression) = visitExpression(thisExpression, null)
    override fun visitThisExpression(thisExpression: JKThisExpression, data: Nothing?) = visitThisExpression(thisExpression)
    fun visitSuperExpression(superExpression: JKSuperExpression) = visitExpression(superExpression, null)
    override fun visitSuperExpression(superExpression: JKSuperExpression, data: Nothing?) = visitSuperExpression(superExpression)
    fun visitWhileStatement(whileStatement: JKWhileStatement) = visitLoopStatement(whileStatement, null)
    override fun visitWhileStatement(whileStatement: JKWhileStatement, data: Nothing?) = visitWhileStatement(whileStatement)
    fun visitDoWhileStatement(doWhileStatement: JKDoWhileStatement) = visitLoopStatement(doWhileStatement, null)
    override fun visitDoWhileStatement(doWhileStatement: JKDoWhileStatement, data: Nothing?) = visitDoWhileStatement(doWhileStatement)
    fun visitBreakStatement(breakStatement: JKBreakStatement) = visitStatement(breakStatement, null)
    override fun visitBreakStatement(breakStatement: JKBreakStatement, data: Nothing?) = visitBreakStatement(breakStatement)
    fun visitBreakWithLabelStatement(breakWithLabelStatement: JKBreakWithLabelStatement) = visitBreakStatement(breakWithLabelStatement, null)
    override fun visitBreakWithLabelStatement(breakWithLabelStatement: JKBreakWithLabelStatement, data: Nothing?) = visitBreakWithLabelStatement(breakWithLabelStatement)
    fun visitIfStatement(ifStatement: JKIfStatement) = visitStatement(ifStatement, null)
    override fun visitIfStatement(ifStatement: JKIfStatement, data: Nothing?) = visitIfStatement(ifStatement)
    fun visitIfElseStatement(ifElseStatement: JKIfElseStatement) = visitIfStatement(ifElseStatement, null)
    override fun visitIfElseStatement(ifElseStatement: JKIfElseStatement, data: Nothing?) = visitIfElseStatement(ifElseStatement)
    fun visitIfElseExpression(ifElseExpression: JKIfElseExpression) = visitExpression(ifElseExpression, null)
    override fun visitIfElseExpression(ifElseExpression: JKIfElseExpression, data: Nothing?) = visitIfElseExpression(ifElseExpression)
    fun visitAssignableExpression(assignableExpression: JKAssignableExpression) = visitExpression(assignableExpression, null)
    override fun visitAssignableExpression(assignableExpression: JKAssignableExpression, data: Nothing?) = visitAssignableExpression(assignableExpression)
    fun visitLambdaExpression(lambdaExpression: JKLambdaExpression) = visitExpression(lambdaExpression, null)
    override fun visitLambdaExpression(lambdaExpression: JKLambdaExpression, data: Nothing?) = visitLambdaExpression(lambdaExpression)
    fun visitDelegationConstructorCall(delegationConstructorCall: JKDelegationConstructorCall) = visitMethodCallExpression(delegationConstructorCall, null)
    override fun visitDelegationConstructorCall(delegationConstructorCall: JKDelegationConstructorCall, data: Nothing?) = visitDelegationConstructorCall(delegationConstructorCall)
    fun visitJavaField(javaField: JKJavaField) = visitField(javaField, null)
    override fun visitJavaField(javaField: JKJavaField, data: Nothing?) = visitJavaField(javaField)
    fun visitJavaMethod(javaMethod: JKJavaMethod) = visitMethod(javaMethod, null)
    override fun visitJavaMethod(javaMethod: JKJavaMethod, data: Nothing?) = visitJavaMethod(javaMethod)
    fun visitJavaMethodCallExpression(javaMethodCallExpression: JKJavaMethodCallExpression) = visitMethodCallExpression(javaMethodCallExpression, null)
    override fun visitJavaMethodCallExpression(javaMethodCallExpression: JKJavaMethodCallExpression, data: Nothing?) = visitJavaMethodCallExpression(javaMethodCallExpression)
    fun visitJavaNewExpression(javaNewExpression: JKJavaNewExpression) = visitExpression(javaNewExpression, null)
    override fun visitJavaNewExpression(javaNewExpression: JKJavaNewExpression, data: Nothing?) = visitJavaNewExpression(javaNewExpression)
    fun visitJavaDefaultNewExpression(javaDefaultNewExpression: JKJavaDefaultNewExpression) = visitExpression(javaDefaultNewExpression, null)
    override fun visitJavaDefaultNewExpression(javaDefaultNewExpression: JKJavaDefaultNewExpression, data: Nothing?) = visitJavaDefaultNewExpression(javaDefaultNewExpression)
    fun visitJavaModifier(javaModifier: JKJavaModifier) = visitModifier(javaModifier, null)
    override fun visitJavaModifier(javaModifier: JKJavaModifier, data: Nothing?) = visitJavaModifier(javaModifier)
    fun visitJavaNewEmptyArray(javaNewEmptyArray: JKJavaNewEmptyArray) = visitExpression(javaNewEmptyArray, null)
    override fun visitJavaNewEmptyArray(javaNewEmptyArray: JKJavaNewEmptyArray, data: Nothing?) = visitJavaNewEmptyArray(javaNewEmptyArray)
    fun visitJavaNewArray(javaNewArray: JKJavaNewArray) = visitExpression(javaNewArray, null)
    override fun visitJavaNewArray(javaNewArray: JKJavaNewArray, data: Nothing?) = visitJavaNewArray(javaNewArray)
    fun visitJavaLiteralExpression(javaLiteralExpression: JKJavaLiteralExpression) = visitLiteralExpression(javaLiteralExpression, null)
    override fun visitJavaLiteralExpression(javaLiteralExpression: JKJavaLiteralExpression, data: Nothing?) = visitJavaLiteralExpression(javaLiteralExpression)
    fun visitReturnStatement(returnStatement: JKReturnStatement) = visitStatement(returnStatement, null)
    override fun visitReturnStatement(returnStatement: JKReturnStatement, data: Nothing?) = visitReturnStatement(returnStatement)
    fun visitJavaAssertStatement(javaAssertStatement: JKJavaAssertStatement) = visitStatement(javaAssertStatement, null)
    override fun visitJavaAssertStatement(javaAssertStatement: JKJavaAssertStatement, data: Nothing?) = visitJavaAssertStatement(javaAssertStatement)
    fun visitJavaForLoopStatement(javaForLoopStatement: JKJavaForLoopStatement) = visitLoopStatement(javaForLoopStatement, null)
    override fun visitJavaForLoopStatement(javaForLoopStatement: JKJavaForLoopStatement, data: Nothing?) = visitJavaForLoopStatement(javaForLoopStatement)
    fun visitJavaInstanceOfExpression(javaInstanceOfExpression: JKJavaInstanceOfExpression) = visitExpression(javaInstanceOfExpression, null)
    override fun visitJavaInstanceOfExpression(javaInstanceOfExpression: JKJavaInstanceOfExpression, data: Nothing?) = visitJavaInstanceOfExpression(javaInstanceOfExpression)
    fun visitJavaPolyadicExpression(javaPolyadicExpression: JKJavaPolyadicExpression) = visitExpression(javaPolyadicExpression, null)
    override fun visitJavaPolyadicExpression(javaPolyadicExpression: JKJavaPolyadicExpression, data: Nothing?) = visitJavaPolyadicExpression(javaPolyadicExpression)
    fun visitJavaAssignmentExpression(javaAssignmentExpression: JKJavaAssignmentExpression) = visitExpression(javaAssignmentExpression, null)
    override fun visitJavaAssignmentExpression(javaAssignmentExpression: JKJavaAssignmentExpression, data: Nothing?) = visitJavaAssignmentExpression(javaAssignmentExpression)
    fun visitJavaSwitchStatement(javaSwitchStatement: JKJavaSwitchStatement) = visitStatement(javaSwitchStatement, null)
    override fun visitJavaSwitchStatement(javaSwitchStatement: JKJavaSwitchStatement, data: Nothing?) = visitJavaSwitchStatement(javaSwitchStatement)
    fun visitJavaSwitchCase(javaSwitchCase: JKJavaSwitchCase) = visitTreeElement(javaSwitchCase, null)
    override fun visitJavaSwitchCase(javaSwitchCase: JKJavaSwitchCase, data: Nothing?) = visitJavaSwitchCase(javaSwitchCase)
    fun visitJavaDefaultSwitchCase(javaDefaultSwitchCase: JKJavaDefaultSwitchCase) = visitJavaSwitchCase(javaDefaultSwitchCase, null)
    override fun visitJavaDefaultSwitchCase(javaDefaultSwitchCase: JKJavaDefaultSwitchCase, data: Nothing?) = visitJavaDefaultSwitchCase(javaDefaultSwitchCase)
    fun visitJavaLabelSwitchCase(javaLabelSwitchCase: JKJavaLabelSwitchCase) = visitJavaSwitchCase(javaLabelSwitchCase, null)
    override fun visitJavaLabelSwitchCase(javaLabelSwitchCase: JKJavaLabelSwitchCase, data: Nothing?) = visitJavaLabelSwitchCase(javaLabelSwitchCase)
    fun visitKtProperty(ktProperty: JKKtProperty) = visitField(ktProperty, null)
    override fun visitKtProperty(ktProperty: JKKtProperty, data: Nothing?) = visitKtProperty(ktProperty)
    fun visitKtFunction(ktFunction: JKKtFunction) = visitMethod(ktFunction, null)
    override fun visitKtFunction(ktFunction: JKKtFunction, data: Nothing?) = visitKtFunction(ktFunction)
    fun visitKtConstructor(ktConstructor: JKKtConstructor) = visitDeclaration(ktConstructor, null)
    override fun visitKtConstructor(ktConstructor: JKKtConstructor, data: Nothing?) = visitKtConstructor(ktConstructor)
    fun visitKtPrimaryConstructor(ktPrimaryConstructor: JKKtPrimaryConstructor) = visitKtConstructor(ktPrimaryConstructor, null)
    override fun visitKtPrimaryConstructor(ktPrimaryConstructor: JKKtPrimaryConstructor, data: Nothing?) = visitKtPrimaryConstructor(ktPrimaryConstructor)
    fun visitKtAssignmentStatement(ktAssignmentStatement: JKKtAssignmentStatement) = visitStatement(ktAssignmentStatement, null)
    override fun visitKtAssignmentStatement(ktAssignmentStatement: JKKtAssignmentStatement, data: Nothing?) = visitKtAssignmentStatement(ktAssignmentStatement)
    fun visitKtCall(ktCall: JKKtCall) = visitMethodCallExpression(ktCall, null)
    override fun visitKtCall(ktCall: JKKtCall, data: Nothing?) = visitKtCall(ktCall)
    fun visitKtModifier(ktModifier: JKKtModifier) = visitModifier(ktModifier, null)
    override fun visitKtModifier(ktModifier: JKKtModifier, data: Nothing?) = visitKtModifier(ktModifier)
    fun visitKtMethodCallExpression(ktMethodCallExpression: JKKtMethodCallExpression) = visitMethodCallExpression(ktMethodCallExpression, null)
    override fun visitKtMethodCallExpression(ktMethodCallExpression: JKKtMethodCallExpression, data: Nothing?) = visitKtMethodCallExpression(ktMethodCallExpression)
    fun visitKtAlsoCallExpression(ktAlsoCallExpression: JKKtAlsoCallExpression) = visitKtMethodCallExpression(ktAlsoCallExpression, null)
    override fun visitKtAlsoCallExpression(ktAlsoCallExpression: JKKtAlsoCallExpression, data: Nothing?) = visitKtAlsoCallExpression(ktAlsoCallExpression)
    fun visitKtLiteralExpression(ktLiteralExpression: JKKtLiteralExpression) = visitLiteralExpression(ktLiteralExpression, null)
    override fun visitKtLiteralExpression(ktLiteralExpression: JKKtLiteralExpression, data: Nothing?) = visitKtLiteralExpression(ktLiteralExpression)
    fun visitKtWhenStatement(ktWhenStatement: JKKtWhenStatement) = visitStatement(ktWhenStatement, null)
    override fun visitKtWhenStatement(ktWhenStatement: JKKtWhenStatement, data: Nothing?) = visitKtWhenStatement(ktWhenStatement)
    fun visitKtWhenCase(ktWhenCase: JKKtWhenCase) = visitTreeElement(ktWhenCase, null)
    override fun visitKtWhenCase(ktWhenCase: JKKtWhenCase, data: Nothing?) = visitKtWhenCase(ktWhenCase)
    fun visitKtWhenLabel(ktWhenLabel: JKKtWhenLabel) = visitTreeElement(ktWhenLabel, null)
    override fun visitKtWhenLabel(ktWhenLabel: JKKtWhenLabel, data: Nothing?) = visitKtWhenLabel(ktWhenLabel)
    fun visitKtElseWhenLabel(ktElseWhenLabel: JKKtElseWhenLabel) = visitKtWhenLabel(ktElseWhenLabel, null)
    override fun visitKtElseWhenLabel(ktElseWhenLabel: JKKtElseWhenLabel, data: Nothing?) = visitKtElseWhenLabel(ktElseWhenLabel)
    fun visitKtValueWhenLabel(ktValueWhenLabel: JKKtValueWhenLabel) = visitKtWhenLabel(ktValueWhenLabel, null)
    override fun visitKtValueWhenLabel(ktValueWhenLabel: JKKtValueWhenLabel, data: Nothing?) = visitKtValueWhenLabel(ktValueWhenLabel)
}
