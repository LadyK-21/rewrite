package org.openrewrite.java.tree;

public interface Dispatcher1<T,P> {

    T dispatchAnnotatedType(J.AnnotatedType annotatedType, P p);

    T dispatchAnnotation(J.Annotation annotation, P p);

    T dispatchArrayAccess(J.ArrayAccess arrayAccess, P p);

    T dispatchArrayType(J.ArrayType arrayType, P p);

    T dispatchAssert(J.Assert anAssert, P p);

    T dispatchAssignment(J.Assignment assignment, P p);

    T dispatchAssignmentOperation(J.AssignmentOperation assignmentOperation, P p);

    T dispatchBinary(J.Binary binary, P p);

    T dispatchBlock(J.Block block, P p);

    T dispatchBreak(J.Break aBreak, P p);

    T dispatchCase(J.Case aCase, P p);

    T dispatchClassDeclaration(J.ClassDeclaration classDeclaration, P p);

    T dispatchCompilationUnit(J.CompilationUnit compilationUnit, P p);

    T dispatchContinue(J.Continue aContinue, P p);

    T dispatchDoWhileLoop(J.DoWhileLoop doWhileLoop, P p);

    T dispatchEmpty(J.Empty empty, P p);

    T dispatchEnumValue(J.EnumValue enumValue, P p);

    T dispatchEnumValueSet(J.EnumValueSet enumValueSet, P p);

    T dispatchFieldAccess(J.FieldAccess fieldAccess, P p);

    T dispatchForEachLoop(J.ForEachLoop forEachLoop, P p);

    T dispatchForEachControl(J.ForEachLoop.Control control, P p);

    T dispatchForLoop(J.ForLoop forLoop, P p);

    T dispatchForControl(J.ForLoop.Control control, P p);

    T dispatchIdentifier(J.Identifier identifier, P p);

    T dispatchIf(J.If anIf, P p);

    T dispatchElse(J.If.Else anElse, P p);

    T dispatchImport(J.Import anImport, P p);

    T dispatchInstanceOf(J.InstanceOf instanceOf, P p);

    T dispatchKind(J.ClassDeclaration.Kind kind, P p);

    T dispatchLabel(J.Label label, P p);

    T dispatchLambda(J.Lambda lambda, P p);

    T dispatchLiteral(J.Literal literal, P p);

    T dispatchMemberReference(J.MemberReference memberReference, P p);

    T dispatchMethodDeclaration(J.MethodDeclaration methodDeclaration, P p);

    T dispatchMultiCatch(J.MultiCatch multiCatch, P p);

    T dispatchNewArray(J.NewArray newArray, P p);

    T dispatchArrayDimension(J.ArrayDimension arrayDimension, P p);

    T dispatchNewClass(J.NewClass newClass, P p);

    T dispatchPackage(J.Package aPackage, P p);

    T dispatchParameterizedType(J.ParameterizedType parameterizedType, P p);

    <J2 extends J> T dispatchParentheses(J.Parentheses<J2> j2Parentheses, P p);

    <J2 extends J> T dispatchControlParentheses(J.ControlParentheses<J2> j2ControlParentheses, P p);

    T dispatchPrimitive(J.Primitive primitive, P p);

    T dispatchReturn(J.Return aReturn, P p);

    T dispatchSwitch(J.Switch aSwitch, P p);

    T dispatchSynchronized(J.Synchronized aSynchronized, P p);

    T dispatchTernary(J.Ternary ternary, P p);

    T dispatchThrow(J.Throw aThrow, P p);

    T dispatchTry(J.Try aTry, P p);

    T dispatchTryResource(J.Try.Resource resource, P p);

    T dispatchCatch(J.Try.Catch aCatch, P p);

    T dispatchTypeCast(J.TypeCast typeCast, P p);

    T dispatchTypeParameter(J.TypeParameter typeParameter, P p);

    T dispatchTypeParameters(J.TypeParameters typeParameters, P p);

    T dispatchUnary(J.Unary unary, P p);

    T dispatchVariableDeclarations(J.VariableDeclarations variableDeclarations, P p);

    T dispatchVariable(J.VariableDeclarations.NamedVariable namedVariable, P p);

    T dispatchWhileLoop(J.WhileLoop whileLoop, P p);

    T dispatchWildcard(J.Wildcard wildcard, P p);

    T dispatchParameters(J.Lambda.Parameters parameters, P p);

    T dispatchMethodInvocation(J.MethodInvocation methodInvocation, P p);

    T dispatchModifiers(J.Modifier modifier, P p);
}
