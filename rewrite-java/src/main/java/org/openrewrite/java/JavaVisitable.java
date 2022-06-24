package org.openrewrite.java;

import org.openrewrite.Tree;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.java.tree.JavaType;

public interface JavaVisitable<T, P> {

    default T defaultValue(Tree tree, P p) {
        throw new UnsupportedOperationException();
    }

    default T visit(Tree tree, P p) {
        throw new UnsupportedOperationException();
    }

    default T visitAnnotatedType(J.AnnotatedType annotatedType, P p) { return defaultValue(annotatedType, p); }
    default T visitAnnotation(J.Annotation annotation, P p) { return defaultValue(annotation, p); }
    default T visitArrayAccess(J.ArrayAccess arrayAccess, P p) { return defaultValue(arrayAccess, p); }
    default T visitArrayDimension(J.ArrayDimension arrayDimension, P p) { return defaultValue(arrayDimension, p); }
    default T visitArrayType(J.ArrayType arrayType, P p) { return defaultValue(arrayType, p); }
    default T visitAssert(J.Assert azzert, P p) { return defaultValue(azzert, p); }
    default T visitAssignment(J.Assignment assignment, P p) { return defaultValue(assignment, p); }
    default T visitAssignmentOperation(J.AssignmentOperation assignOp, P p) { return defaultValue(assignOp, p); }
    default T visitBinary(J.Binary binary, P p) { return defaultValue(binary, p); }
    default T visitBlock(J.Block block, P p) { return defaultValue(block, p); }
    default T visitBreak(J.Break breakStatement, P p) { return defaultValue(breakStatement, p); }
    default T visitCase(J.Case caze, P p) { return defaultValue(caze, p); }
    default T visitCatch(J.Try.Catch catzh, P p) { return defaultValue(catzh, p); }
    default T visitClassDeclaration(J.ClassDeclaration classDecl, P p) { return defaultValue(classDecl, p); }
    default T visitJavaSourceFile(JavaSourceFile cu, P p) { return defaultValue(cu, p); }
    default T visitCompilationUnit(J.CompilationUnit cu, P p) { return defaultValue(cu, p); }
    default T visitContinue(J.Continue continueStatement, P p) { return defaultValue(continueStatement, p); }
    default <J2 extends J> T visitControlParentheses(J.ControlParentheses<J2> controlParens, P p) { return defaultValue(controlParens, p); }
    default T visitDoWhileLoop(J.DoWhileLoop doWhileLoop, P p) { return defaultValue(doWhileLoop, p); }
    default T visitEmpty(J.Empty empty, P p) { return defaultValue(empty, p); }
    default T visitEnumValue(J.EnumValue enoom, P p) { return defaultValue(enoom, p); }
    default T visitEnumValueSet(J.EnumValueSet enums, P p) { return defaultValue(enums, p); }
    default T visitFieldAccess(J.FieldAccess fieldAccess, P p) { return defaultValue(fieldAccess, p); }
    default T visitForEachLoop(J.ForEachLoop forLoop, P p) { return defaultValue(forLoop, p); }
    default T visitForEachControl(J.ForEachLoop.Control control, P p) { return defaultValue(control, p); }
    default T visitForLoop(J.ForLoop forLoop, P p) { return defaultValue(forLoop, p); }
    default T visitForControl(J.ForLoop.Control control, P p) { return defaultValue(control, p); }
    default T visitIdentifier(J.Identifier ident, P p) { return defaultValue(ident, p); }
    default T visitElse(J.If.Else elze, P p) { return defaultValue(elze, p); }
    default T visitIf(J.If iff, P p) { return defaultValue(iff, p); }
    default T visitImport(J.Import impoort, P p) { return defaultValue(impoort, p); }
    default T visitInstanceOf(J.InstanceOf instanceOf, P p) { return defaultValue(instanceOf, p); }
    default T visitLabel(J.Label label, P p) { return defaultValue(label, p); }
    default T visitLambda(J.Lambda lambda, P p) { return defaultValue(lambda, p); }
    default T visitLiteral(J.Literal literal, P p) { return defaultValue(literal, p); }
    default T visitMemberReference(J.MemberReference memberRef, P p) { return defaultValue(memberRef, p); }
    default T visitMethodDeclaration(J.MethodDeclaration method, P p) { return defaultValue(method, p); }
    default T visitMethodInvocation(J.MethodInvocation method, P p) { return defaultValue(method, p); }
    default T visitMultiCatch(J.MultiCatch multiCatch, P p) { return defaultValue(multiCatch, p); }
    default T visitVariableDeclarations(J.VariableDeclarations multiVariable, P p) { return defaultValue(multiVariable, p); }
    default T visitNewArray(J.NewArray newArray, P p) { return defaultValue(newArray, p); }
    default T visitNewClass(J.NewClass newClass, P p) { return defaultValue(newClass, p); }
    default T visitPackage(J.Package pkg, P p) { return defaultValue(pkg, p); }
    default T visitParameterizedType(J.ParameterizedType type, P p) { return defaultValue(type, p); }
    default <J2 extends J> T visitParentheses(J.Parentheses<J2> parens, P p) { return defaultValue(parens, p); }
    default T visitPrimitive(J.Primitive primitive, P p) { return defaultValue(primitive, p); }
    default T visitReturn(J.Return retrn, P p) { return defaultValue(retrn, p); }
    default T visitSwitch(J.Switch switzh, P p) { return defaultValue(switzh, p); }
    default T visitSynchronized(J.Synchronized synch, P p) { return defaultValue(synch, p); }
    default T visitTernary(J.Ternary ternary, P p) { return defaultValue(ternary, p); }
    default T visitThrow(J.Throw thrown, P p) { return defaultValue(thrown, p); }
    default T visitTry(J.Try tryable, P p) { return defaultValue(tryable, p); }
    default T visitTryResource(J.Try.Resource tryResource, P p) { return defaultValue(tryResource, p); }
    default T visitTypeCast(J.TypeCast typeCast, P p) { return defaultValue(typeCast, p); }
    default T visitTypeParameter(J.TypeParameter typeParam, P p) { return defaultValue(typeParam, p); }
    default T visitUnary(J.Unary unary, P p) { return defaultValue(unary, p); }
    default T visitVariable(J.VariableDeclarations.NamedVariable variable, P p) { return defaultValue(variable, p); }
    default T visitWhileLoop(J.WhileLoop whileLoop, P p) { return defaultValue(whileLoop, p); }
    default T visitWildcard(J.Wildcard wildcard, P p) { return defaultValue(wildcard, p); }

    default JavaType visitType(JavaType javaType, P p) {
        throw new UnsupportedOperationException();
    }
}
