package org.openrewrite.java;

import org.openrewrite.Tree;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.*;

import static com.sun.tools.javac.main.Option.G;

public interface JavaVisitable<T, P> {

    T defaultValue(T tree, P p);

    T visit(Tree tree, P p);

    public T visitAnnotatedType(J.AnnotatedType annotatedType, P p);
    public T visitAnnotation(J.Annotation annotation, P p);
    public T visitArrayAccess(J.ArrayAccess arrayAccess, P p);
    public T visitArrayDimension(J.ArrayDimension arrayDimension, P p);
    public T visitArrayType(J.ArrayType arrayType, P p);
    public T visitAssert(J.Assert azzert, P p);
    public T visitAssignment(J.Assignment assignment, P p);
    public T visitAssignmentOperation(J.AssignmentOperation assignOp, P p);
    public T visitBinary(J.Binary binary, P p);
    public T visitBlock(J.Block block, P p);
    public T visitBreak(J.Break breakStatement, P p);
    public T visitCase(J.Case caze, P p);
    public T visitCatch(J.Try.Catch catzh, P p);
    public T visitClassDeclaration(J.ClassDeclaration classDecl, P p);
    public T visitJavaSourceFile(JavaSourceFile cu, P p);
    public T visitCompilationUnit(J.CompilationUnit cu, P p);
    public T visitContinue(J.Continue continueStatement, P p);
    public <T extends J> J visitControlParentheses(J.ControlParentheses<T> controlParens, P p);
    public T visitDoWhileLoop(J.DoWhileLoop doWhileLoop, P p);
    public T visitEmpty(J.Empty empty, P p);
    public T visitEnumValue(J.EnumValue enoom, P p);
    public T visitEnumValueSet(J.EnumValueSet enums, P p);
    public T visitFieldAccess(J.FieldAccess fieldAccess, P p);
    public T visitForEachLoop(J.ForEachLoop forLoop, P p);
    public T visitForEachControl(J.ForEachLoop.Control control, P p);
    public T visitForLoop(J.ForLoop forLoop, P p);
    public T visitForControl(J.ForLoop.Control control, P p);
    public T visitIdentifier(J.Identifier ident, P p);
    public T visitElse(J.If.Else elze, P p);
    public T visitIf(J.If iff, P p);
    public T visitImport(J.Import impoort, P p);
    public T visitInstanceOf(J.InstanceOf instanceOf, P p);
    public T visitLabel(J.Label label, P p);
    public T visitLambda(J.Lambda lambda, P p);
    public T visitLiteral(J.Literal literal, P p);
    public T visitMemberReference(J.MemberReference memberRef, P p);
    public T visitMethodDeclaration(J.MethodDeclaration method, P p);
    public T visitMethodInvocation(J.MethodInvocation method, P p);
    public T visitMultiCatch(J.MultiCatch multiCatch, P p);
    public T visitVariableDeclarations(J.VariableDeclarations multiVariable, P p);
    public T visitNewArray(J.NewArray newArray, P p);
    public T visitNewClass(J.NewClass newClass, P p);
    public T visitPackage(J.Package pkg, P p);
    public T visitParameterizedType(J.ParameterizedType type, P p);
    public <T extends J> J visitParentheses(J.Parentheses<T> parens, P p);
    public T visitPrimitive(J.Primitive primitive, P p);
    public T visitReturn(J.Return retrn, P p);
    public T visitSwitch(J.Switch switzh, P p);
    public T visitSynchronized(J.Synchronized synch, P p);
    public T visitTernary(J.Ternary ternary, P p);
    public T visitThrow(J.Throw thrown, P p);
    public T visitTry(J.Try tryable, P p);
    public T visitTryResource(J.Try.Resource tryResource, P p);
    public T visitTypeCast(J.TypeCast typeCast, P p);
    public T visitTypeParameter(J.TypeParameter typeParam, P p);
    public T visitUnary(J.Unary unary, P p);
    public T visitVariable(J.VariableDeclarations.NamedVariable variable, P p);
    public T visitWhileLoop(J.WhileLoop whileLoop, P p);
    public T visitWildcard(J.Wildcard wildcard, P p);

    public JavaType visitType(JavaType javaType, P p);
}
