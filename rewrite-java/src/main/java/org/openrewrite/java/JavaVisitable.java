package org.openrewrite.java;

import org.openrewrite.Tree;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;

public interface JavaVisitable<T, P> {

    T defaultValue(Tree tree, P p);

    T visit(Tree tree, P p);

    T visitAnnotatedType(J.AnnotatedType annotatedType, P p);
    T visitAnnotation(J.Annotation annotation, P p);
    T visitArrayAccess(J.ArrayAccess arrayAccess, P p);
    T visitArrayDimension(J.ArrayDimension arrayDimension, P p);
    T visitArrayType(J.ArrayType arrayType, P p);
    T visitAssert(J.Assert azzert, P p);
    T visitAssignment(J.Assignment assignment, P p);
    T visitAssignmentOperation(J.AssignmentOperation assignOp, P p);
    T visitBinary(J.Binary binary, P p);
    T visitBlock(J.Block block, P p);
    T visitBreak(J.Break breakStatement, P p);
    T visitCase(J.Case caze, P p);
    T visitCatch(J.Try.Catch catzh, P p);
    T visitClassDeclaration(J.ClassDeclaration classDecl, P p);
    T visitJavaSourceFile(JavaSourceFile cu, P p);
    T visitCompilationUnit(J.CompilationUnit cu, P p);
    T visitContinue(J.Continue continueStatement, P p);
    <J2 extends J> T visitControlParentheses(J.ControlParentheses<J2> controlParens, P p);
    T visitDoWhileLoop(J.DoWhileLoop doWhileLoop, P p);
    T visitEmpty(J.Empty empty, P p);
    T visitEnumValue(J.EnumValue enoom, P p);
    T visitEnumValueSet(J.EnumValueSet enums, P p);
    T visitFieldAccess(J.FieldAccess fieldAccess, P p);
    T visitForEachLoop(J.ForEachLoop forLoop, P p);
    T visitForEachControl(J.ForEachLoop.Control control, P p);
    T visitForLoop(J.ForLoop forLoop, P p);
    T visitForControl(J.ForLoop.Control control, P p);
    T visitIdentifier(J.Identifier ident, P p);
    T visitElse(J.If.Else elze, P p);
    T visitIf(J.If iff, P p);
    T visitImport(J.Import impoort, P p);
    T visitInstanceOf(J.InstanceOf instanceOf, P p);
    T visitLabel(J.Label label, P p);
    T visitLambda(J.Lambda lambda, P p);
    T visitLiteral(J.Literal literal, P p);
    T visitMemberReference(J.MemberReference memberRef, P p);
    T visitMethodDeclaration(J.MethodDeclaration method, P p);
    T visitMethodInvocation(J.MethodInvocation method, P p);
    T visitMultiCatch(J.MultiCatch multiCatch, P p);
    T visitVariableDeclarations(J.VariableDeclarations multiVariable, P p);
    T visitNewArray(J.NewArray newArray, P p);
    T visitNewClass(J.NewClass newClass, P p);
    T visitPackage(J.Package pkg, P p);
    T visitParameterizedType(J.ParameterizedType type, P p);
    <J2 extends J> T visitParentheses(J.Parentheses<J2> parens, P p);
    T visitPrimitive(J.Primitive primitive, P p);
    T visitReturn(J.Return retrn, P p);
    T visitSwitch(J.Switch switzh, P p);
    T visitSynchronized(J.Synchronized synch, P p);
    T visitTernary(J.Ternary ternary, P p);
    T visitThrow(J.Throw thrown, P p);
    T visitTry(J.Try tryable, P p);
    T visitTryResource(J.Try.Resource tryResource, P p);
    T visitTypeCast(J.TypeCast typeCast, P p);
    T visitTypeParameter(J.TypeParameter typeParam, P p);
    T visitUnary(J.Unary unary, P p);
    T visitVariable(J.VariableDeclarations.NamedVariable variable, P p);
    T visitWhileLoop(J.WhileLoop whileLoop, P p);
    T visitWildcard(J.Wildcard wildcard, P p);

    JavaType visitType(JavaType javaType, P p);
}
