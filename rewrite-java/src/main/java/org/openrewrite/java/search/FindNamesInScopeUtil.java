/*
 * Copyright 2022 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.search;

import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Flag;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.*;

@Incubating(since = "7.25.0")
@Value
public class FindNamesInScopeUtil {

    private static final class VariableNameScopeVisitor extends JavaIsoVisitor<Set<String>> {
        private final Cursor scope;
        private final Map<Cursor, Set<String>> nameScopes;
        private final Stack<Cursor> currentScope;

        public VariableNameScopeVisitor(Cursor scope) {
            this.scope = scope;
            this.nameScopes = new LinkedHashMap<>();
            this.currentScope = new Stack<>();
        }

        @Override
        public Statement visitStatement(Statement statement, Set<String> namesInScope) {
            Statement s = super.visitStatement(statement, namesInScope);
            Cursor parentScope = getParentScope();
            if (currentScope.isEmpty() || currentScope.peek() != parentScope) {
                Set<String> namesInParentScope = nameScopes.computeIfAbsent(parentScope, k -> new HashSet<>());
                if (!currentScope.isEmpty() && parentScope.isScopeInPath(currentScope.peek().getValue())) {
                    namesInParentScope.addAll(nameScopes.get(currentScope.peek()));
                }
                currentScope.push(parentScope);
            }
            return s;
        }

        // Stop after the tree has been processed to ensure all the names in scope have been collected.
        @Override
        public @Nullable J postVisit(J tree, Set<String> namesInScope) {
            if (!currentScope.isEmpty() && currentScope.peek().getValue().equals(tree)) {
                currentScope.pop();
            }

            if (scope.getValue().equals(tree)) {
                Cursor parentScope = getParentScope();
                Set<String> names = nameScopes.get(parentScope);

                // Add the names created in the target scope.
                Set<String> namesInCursorScope = nameScopes.get(scope);
                if (namesInCursorScope != null) {
                    names.addAll(nameScopes.get(scope));
                }
                namesInScope.addAll(names);
                return tree;
            }

            return super.postVisit(tree, namesInScope);
        }

        @Override
        public J.Import visitImport(J.Import _import, Set<String> namesInScope) {
            // Skip identifiers from `import`s.
            return _import;
        }

        @Override
        public J.Package visitPackage(J.Package pkg, Set<String> namesInScope) {
            // Skip identifiers from `package`.
            return pkg;
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, Set<String> namesInScope) {
            // Collect class fields first, because class fields are always visible regardless of what order the statements are declared.
            classDecl.getBody().getStatements().forEach(o -> {
                if (o instanceof J.VariableDeclarations) {
                    J.VariableDeclarations variableDeclarations = (J.VariableDeclarations) o;
                    variableDeclarations.getVariables().forEach(v ->
                            nameScopes.computeIfAbsent(getCursor(), k -> new HashSet<>()).add(v.getSimpleName()));
                }
            });

            addImportedStaticFieldNames(getCursor().firstEnclosing(J.CompilationUnit.class), getCursor());
            if (classDecl.getType() != null) {
                addInheritedClassFields(classDecl.getType().getSupertype(), getCursor());
            }
            return super.visitClassDeclaration(classDecl, namesInScope);
        }

        private void addImportedStaticFieldNames(@Nullable J.CompilationUnit cu, Cursor classCursor) {
            if (cu != null) {
                List<J.Import> imports = cu.getImports();
                imports.forEach(i -> {
                    if (i.isStatic()) {
                        // TODO: Figure out how to add imported field names. Type appears to be unknown, but could be inferred through the select.
                        // Add conditions ...
                        Set<String> namesAtCursor = nameScopes.computeIfAbsent(classCursor, k -> new HashSet<>());
                        namesAtCursor.add(i.getQualid().getSimpleName());
                    }
                });
            }
        }

        private void addInheritedClassFields(@Nullable JavaType.FullyQualified fq, Cursor classCursor) {
            if (fq != null) {
                J.ClassDeclaration cd = classCursor.getValue();
                boolean isSamePackage = cd.getType() != null && cd.getType().getPackageName().equals(fq.getPackageName());
                fq.getMembers().forEach(m -> {
                    if ((Flag.hasFlags(m.getFlagsBitMap(), Flag.Public) ||
                            Flag.hasFlags(m.getFlagsBitMap(), Flag.Protected)) ||
                            // Member is accessible as package-private.
                            !Flag.hasFlags(m.getFlagsBitMap(), Flag.Private) && isSamePackage) {
                        Set<String> namesAtCursor = nameScopes.computeIfAbsent(classCursor, k -> new HashSet<>());
                        namesAtCursor.add(m.getName());
                    }
                });
                addInheritedClassFields(fq.getSupertype(), classCursor);
            }
        }

        @Override
        public J.Identifier visitIdentifier(J.Identifier identifier, Set<String> namesInScope) {
            if (isValidIdentifier()) {
                Set<String> names = nameScopes.get(currentScope.peek());
                if (names != null) {
                    names.add(identifier.getSimpleName());
                }
            }
            return super.visitIdentifier(identifier, namesInScope);
        }

        // Sets structure for cursor positions to aggregate namespaces.
        private Cursor getParentScope() {
            return getCursor().dropParentUntil(is ->
                    is instanceof J.CompilationUnit ||
                    is instanceof J.ClassDeclaration ||
                    is instanceof J.MethodDeclaration ||
                    is instanceof J.Block ||
                    is instanceof J.ForLoop ||
                    is instanceof J.Case ||
                    is instanceof J.Try ||
                    is instanceof J.Try.Catch ||
                    is instanceof J.If ||
                    is instanceof J.If.Else ||
                    is instanceof J.Lambda);
        }

        // Filter out identifiers that won't create namespace conflicts.
        private boolean isValidIdentifier() {
            J parent = getCursor().dropParentUntil(is -> is instanceof J).getValue();
            return !(parent instanceof J.ClassDeclaration) &&
                    !(parent instanceof J.MethodDeclaration) &&
                    !(parent instanceof J.MethodInvocation) &&
                    !(parent instanceof J.VariableDeclarations) &&
                    !(parent instanceof J.NewClass) &&
                    !(parent instanceof J.Annotation) &&
                    !(parent instanceof J.MultiCatch) &&
                    !(parent instanceof J.ParameterizedType);
        }
    }

    // The J.CompilationUnit is used to ensure all J.ClassDeclaration fields may be found.
    public static Set<String> findVariableNamesInScope(J.CompilationUnit compilationUnit, Cursor scope) {
        Set<String> names = new HashSet<>();
        VariableNameScopeVisitor variableNameScopeVisitor = new VariableNameScopeVisitor(scope);
        variableNameScopeVisitor.visit(compilationUnit, names);
        return names;
    }
}
