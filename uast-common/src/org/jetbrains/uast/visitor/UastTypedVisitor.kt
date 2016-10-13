package org.jetbrains.uast.visitor

import org.jetbrains.uast.*
import org.jetbrains.uast.expressions.UReferenceExpression
import org.jetbrains.uast.expressions.UTypeReferenceExpression

interface UastTypedVisitor<in D, out R> {
    fun visitElement(node: UElement, data: D): R
    // Just elements
    fun visitFile(node: UFile, data: D): R = visitElement(node, data)
    fun visitImportStatement(node: UImportStatement, data: D): R = visitElement(node, data)
    fun visitAnnotation(node: UAnnotation, data: D): R = visitElement(node, data)
    fun visitCatchClause(node: UCatchClause, data: D) = visitElement(node, data)
    // Declarations
    fun visitDeclaration(node: UDeclaration, data: D) = visitElement(node, data)
    fun visitClass(node: UClass, data: D): R = visitDeclaration(node, data)
    fun visitMethod(node: UMethod, data: D): R = visitDeclaration(node, data)
    fun visitClassInitializer(node: UClassInitializer, data: D): R = visitDeclaration(node, data)
    // Variables
    fun visitVariable(node: UVariable, data: D): R = visitDeclaration(node, data)
    fun visitParameter(node: UParameter, data: D): R = visitVariable(node, data)
    fun visitField(node: UField, data: D): R = visitVariable(node, data)
    fun visitLocalVariable(node: ULocalVariable, data: D): R = visitVariable(node, data)
    fun visitEnumConstantExpression(node: UEnumConstant, data: D) = visitVariable(node, data)
    // Expressions
    fun visitExpression(node: UExpression, data: D) = visitElement(node, data)
    fun visitLabeledExpression(node: ULabeledExpression, data: D) = visitExpression(node, data)
    fun visitDeclarationsExpression(node: UVariableDeclarationsExpression, data: D) = visitExpression(node, data)
    fun visitBlockExpression(node: UBlockExpression, data: D) = visitExpression(node, data)
    fun visitTypeReferenceExpression(node: UTypeReferenceExpression, data: D) = visitExpression(node, data)
    fun visitExpressionList(node: UExpressionList, data: D) = visitExpression(node, data)
    fun visitLiteralExpression(node: ULiteralExpression, data: D) = visitExpression(node, data)
    fun visitThisExpression(node: UThisExpression, data: D) = visitExpression(node, data)
    fun visitSuperExpression(node: USuperExpression, data: D) = visitExpression(node, data)
    fun visitArrayAccessExpression(node: UArrayAccessExpression, data: D) = visitExpression(node, data)
    fun visitCallableReferenceExpression(node: UCallableReferenceExpression, data: D) = visitExpression(node, data)
    fun visitClassLiteralExpression(node: UClassLiteralExpression, data: D) = visitExpression(node, data)
    fun visitLambdaExpression(node: ULambdaExpression, data: D) = visitExpression(node, data)
    // Calls
    fun visitCallExpression(node: UCallExpression, data: D) = visitExpression(node, data)
    fun visitObjectLiteralExpression(node: UObjectLiteralExpression, data: D) = visitCallExpression(node, data)
    // Operations
    fun visitBinaryExpression(node: UBinaryExpression, data: D) = visitExpression(node, data)
    fun visitBinaryExpressionWithType(node: UBinaryExpressionWithType, data: D) = visitExpression(node, data)
    fun visitParenthesizedExpression(node: UParenthesizedExpression, data: D) = visitExpression(node, data)
    // Unary operations
    fun visitUnaryExpression(node: UUnaryExpression, data: D) = visitExpression(node, data)
    fun visitPrefixExpression(node: UPrefixExpression, data: D) = visitUnaryExpression(node, data)
    fun visitPostfixExpression(node: UPostfixExpression, data: D) = visitUnaryExpression(node, data)
    // References
    fun visitReferenceExpression(node: UReferenceExpression, data: D) = visitExpression(node, data)
    fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression, data: D) = visitReferenceExpression(node, data)
    fun visitSimpleNameReferenceExpression(node: USimpleNameReferenceExpression, data: D) = visitReferenceExpression(node, data)
    // Control structures
    fun visitIfExpression(node: UIfExpression, data: D) = visitExpression(node, data)
    fun visitSwitchExpression(node: USwitchExpression, data: D) = visitExpression(node, data)
    fun visitSwitchClauseExpression(node: USwitchClauseExpression, data: D) = visitExpression(node, data)
    fun visitTryExpression(node: UTryExpression, data: D) = visitExpression(node, data)
    // Jumps
    fun visitReturnExpression(node: UReturnExpression, data: D) = visitExpression(node, data)
    fun visitBreakExpression(node: UBreakExpression, data: D) = visitExpression(node, data)
    fun visitContinueExpression(node: UContinueExpression, data: D) = visitExpression(node, data)
    fun visitThrowExpression(node: UThrowExpression, data: D) = visitExpression(node, data)
    // Loops
    fun visitLoopExpression(node: ULoopExpression, data: D) = visitExpression(node, data)
    fun visitWhileExpression(node: UWhileExpression, data: D) = visitLoopExpression(node, data)
    fun visitDoWhileExpression(node: UDoWhileExpression, data: D) = visitLoopExpression(node, data)
    fun visitForExpression(node: UForExpression, data: D) = visitLoopExpression(node, data)
    fun visitForEachExpression(node: UForEachExpression, data: D) = visitLoopExpression(node, data)
}