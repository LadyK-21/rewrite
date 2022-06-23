package org.openrewrite.java;

import org.openrewrite.Tree;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;

public class JavaVisitableAdapter<T, P> implements JavaVisitable<T, P> {

    public T defaultValue(J tree, P p) {
        throw new UnsupportedOperationException();
    }

    public T visit(Tree tree, P p) {
        throw new UnsupportedOperationException();
    }

    public T visitAnnotatedType(J.AnnotatedType annotatedType, P p) { return defaultValue(annotatedType, p); }
    public T visitAnnotation(J.Annotation annotation, P p) { return defaultValue(annotation, p); }
    public T visitArrayAccess(J.ArrayAccess arrayAccess, P p) { return defaultValue(arrayAccess, p); }
    public T visitArrayDimension(J.ArrayDimension arrayDimension, P p) { return defaultValue(arrayDimension, p); }
    public T visitArrayType(J.ArrayType arrayType, P p) { return defaultValue(arrayType, p); }
    public T visitAssert(J.Assert azzert, P p) { return defaultValue(azzert, p); }
    public T visitAssignment(J.Assignment assignment, P p) { return defaultValue(assignment, p); }
    public T visitAssignmentOperation(J.AssignmentOperation assignOp, P p) { return defaultValue(assignOp, p); }
    public T visitBinary(J.Binary binary, P p) { return defaultValue(binary, p); }
    public T visitBlock(J.Block block, P p) { return defaultValue(block, p); }
    public T visitBreak(J.Break breakStatement, P p) { return defaultValue(breakStatement, p); }
    public T visitCase(J.Case caze, P p) { return defaultValue(caze, p); }
    public T visitCatch(J.Try.Catch catzh, P p) { return defaultValue(catzh, p); }
    public T visitClassDeclaration(J.ClassDeclaration classDecl, P p) { return defaultValue(classDecl, p); }
    public T visitJavaSourceFile(JavaSourceFile cu, P p) { return defaultValue(cu, p); }
    public T visitCompilationUnit(J.CompilationUnit cu, P p) { return defaultValue(cu, p); }
    public T visitContinue(J.Continue continueStatement, P p) { return defaultValue(continueStatement, p); }
    public T visitControlParentheses(J.ControlParentheses controlParens, P p) { return defaultValue(controlParens, p); }
    public T visitDoWhileLoop(J.DoWhileLoop doWhileLoop, P p) { return defaultValue(doWhileLoop, p); }
    public T visitEmpty(J.Empty empty, P p) { return defaultValue(empty, p); }
    public T visitEnumValue(J.EnumValue enoom, P p) { return defaultValue(enoom, p); }
    public T visitEnumValueSet(J.EnumValueSet enums, P p) { return defaultValue(enums, p); }
    public T visitFieldAccess(J.FieldAccess fieldAccess, P p) { return defaultValue(fieldAccess, p); }
    public T visitForEachLoop(J.ForEachLoop forLoop, P p) { return defaultValue(forLoop, p); }
    public T visitForEachControl(J.ForEachLoop.Control control, P p) { return defaultValue(control, p); }
    public T visitForLoop(J.ForLoop forLoop, P p) { return defaultValue(forLoop, p); }
    public T visitForControl(J.ForLoop.Control control, P p) { return defaultValue(control, p); }
    public T visitIdentifier(J.Identifier ident, P p) { return defaultValue(ident, p); }
    public T visitElse(J.If.Else elze, P p) { return defaultValue(elze, p); }
    public T visitIf(J.If iff, P p) { return defaultValue(iff, p); }
    public T visitImport(J.Import impoort, P p) { return defaultValue(impoort, p); }
    public T visitInstanceOf(J.InstanceOf instanceOf, P p) { return defaultValue(instanceOf, p); }
    public T visitLabel(J.Label label, P p) { return defaultValue(label, p); }
    public T visitLambda(J.Lambda lambda, P p) { return defaultValue(lambda, p); }
    public T visitLiteral(J.Literal literal, P p) { return defaultValue(literal, p); }
    public T visitMemberReference(J.MemberReference memberRef, P p) { return defaultValue(memberRef, p); }
    public T visitMethodDeclaration(J.MethodDeclaration method, P p) { return defaultValue(method, p); }
    public T visitMethodInvocation(J.MethodInvocation method, P p) { return defaultValue(method, p); }
    public T visitMultiCatch(J.MultiCatch multiCatch, P p) { return defaultValue(multiCatch, p); }
    public T visitVariableDeclarations(J.VariableDeclarations multiVariable, P p) { return defaultValue(multiVariable, p); }
    public T visitNewArray(J.NewArray newArray, P p) { return defaultValue(newArray, p); }
    public T visitNewClass(J.NewClass newClass, P p) { return defaultValue(newClass, p); }
    public T visitPackage(J.Package pkg, P p) { return defaultValue(pkg, p); }
    public T visitParameterizedType(J.ParameterizedType type, P p) { return defaultValue(type, p); }
    public T visitParentheses(J.Parentheses parens, P p) { return defaultValue(parens, p); }
    public T visitPrimitive(J.Primitive primitive, P p) { return defaultValue(primitive, p); }
    public T visitReturn(J.Return retrn, P p) { return defaultValue(retrn, p); }
    public T visitSwitch(J.Switch switzh, P p) { return defaultValue(switzh, p); }
    public T visitSynchronized(J.Synchronized synch, P p) { return defaultValue(synch, p); }
    public T visitTernary(J.Ternary ternary, P p) { return defaultValue(ternary, p); }
    public T visitThrow(J.Throw thrown, P p) { return defaultValue(thrown, p); }
    public T visitTry(J.Try tryable, P p) { return defaultValue(tryable, p); }
    public T visitTryResource(J.Try.Resource tryResource, P p) { return defaultValue(tryResource, p); }
    public T visitTypeCast(J.TypeCast typeCast, P p) { return defaultValue(typeCast, p); }
    public T visitTypeParameter(J.TypeParameter typeParam, P p) { return defaultValue(typeParam, p); }
    public T visitUnary(J.Unary unary, P p) { return defaultValue(unary, p); }
    public T visitVariable(J.VariableDeclarations.NamedVariable variable, P p) { return defaultValue(variable, p); }
    public T visitWhileLoop(J.WhileLoop whileLoop, P p) { return defaultValue(whileLoop, p); }
    public T visitWildcard(J.Wildcard wildcard, P p) { return defaultValue(wildcard, p); }

    public JavaType visitType(JavaType javaType, P p) {
        throw new UnsupportedOperationException();
    }
}
