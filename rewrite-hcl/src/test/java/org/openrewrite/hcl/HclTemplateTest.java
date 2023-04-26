/*
 * Copyright 2021 the original author or authors.
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
package org.openrewrite.hcl;

import org.junit.jupiter.api.Test;
import org.openrewrite.ExecutionContext;
import org.openrewrite.hcl.tree.Expression;
import org.openrewrite.hcl.tree.Hcl;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RewriteTest;

import java.util.Comparator;

import static java.util.Objects.requireNonNull;
import static org.openrewrite.hcl.Assertions.hcl;

class HclTemplateTest implements RewriteTest {

    @DocumentExample
    @Test
    void lastInConfigFile() {
        rewriteRun(
          spec -> spec.recipe(RewriteTest.toRecipe(() -> new HclVisitor<ExecutionContext>() {
              final HclTemplate t = HclTemplate.builder(this::getCursor, "after {\n}").build();

              @Override
              public Hcl visitConfigFile(Hcl.ConfigFile configFile, ExecutionContext p) {
                  if (configFile.getBody().size() == 1) {
                      return configFile.withTemplate(t, configFile.getCoordinates().last());
                  }
                  return super.visitConfigFile(configFile, p);
              }
          })),
          hcl(
            """
              before {
              }
              """,
            """
              before {
              }
              after {
              }
              """
          )
        );
    }

    @Test
    void firstInConfigFile() {
        rewriteRun(
          spec -> spec.recipe(RewriteTest.toRecipe(() -> new HclVisitor<ExecutionContext>() {
              final HclTemplate t = HclTemplate.builder(this::getCursor, "after {\n}").build();

              @Override
              public Hcl visitConfigFile(Hcl.ConfigFile configFile, ExecutionContext p) {
                  if (configFile.getBody().size() == 1) {
                      return configFile.withTemplate(t, configFile.getCoordinates().first());
                  }
                  return super.visitConfigFile(configFile, p);
              }
          })),
          hcl(
            """
              before {
              }
              """,
            """
              after {
              }
              before {
              }
              """
          )
        );
    }

    @Test
    void middleOfConfigFile() {
        rewriteRun(
          spec -> spec.recipe(RewriteTest.toRecipe(() -> new HclVisitor<ExecutionContext>() {
              final HclTemplate t = HclTemplate.builder(this::getCursor, "second {\n}").build();

              @Override
              public Hcl visitConfigFile(Hcl.ConfigFile configFile, ExecutionContext p) {
                  if (configFile.getBody().size() == 2) {
                      return configFile.withTemplate(t, configFile.getCoordinates().add(
                        Comparator.comparing(bc -> requireNonNull(((Hcl.Block) bc).getType()).getName())));
                  }
                  return super.visitConfigFile(configFile, p);
              }
          })),
          hcl(
            """
              first {
              }
              third {
              }
              """,
            """
              first {
              }
              second {
              }
              third {
              }
              """
          )
        );
    }

    @Test
    void lastBodyContentInBlock() {
        rewriteRun(
          spec -> spec.recipe(RewriteTest.toRecipe(() -> new HclVisitor<ExecutionContext>() {
              final HclTemplate t = HclTemplate.builder(this::getCursor, "encrypted = true").build();

              @Override
              public Hcl visitBlock(Hcl.Block block, ExecutionContext p) {
                  if (block.getBody().size() == 1) {
                      return block.withTemplate(t, block.getCoordinates().last());
                  }
                  return super.visitBlock(block, p);
              }
          })),
          hcl(
            """
              resource "aws_ebs_volume" {
                size = 1
              }
              """,
            """
              resource "aws_ebs_volume" {
                size      = 1
                encrypted = true
              }
              """
          )
        );
    }

    @Test
    void replaceBlock() {
        rewriteRun(
          spec -> spec.recipe(RewriteTest.toRecipe(() -> new HclVisitor<ExecutionContext>() {
              final HclTemplate t = HclTemplate.builder(
                this::getCursor, """
                      resource "azure_storage_volume" {
                        size = 1
                      }
                  """
              ).build();

              @Override
              public Hcl visitBlock(Hcl.Block block, ExecutionContext p) {
                  if (((Hcl.Literal) block.getLabels().get(0)).getValueSource().contains("aws")) {
                      return block.withTemplate(t, block.getCoordinates().replace());
                  }
                  return super.visitBlock(block, p);
              }
          })),
          hcl(
            """
              resource "aws_ebs_volume" {
                size = 1
              }
              """,
            """
              resource "azure_storage_volume" {
                size = 1
              }
              """
          )
        );
    }

    @Test
    void replaceExpression() {
        rewriteRun(
          spec -> spec.recipe(RewriteTest.toRecipe(() -> new HclVisitor<ExecutionContext>() {
              final HclTemplate t = HclTemplate.builder(this::getCursor, "\"jonathan\"").build();

              @Override
              public Hcl visitExpression(Expression expression, ExecutionContext p) {
                  if (expression instanceof Hcl.QuotedTemplate && expression.print(getCursor().getParentOrThrow()).contains("you")) {
                      return expression.withTemplate(t, expression.getCoordinates().replace());
                  }
                  return super.visitExpression(expression, p);
              }
          })),
          hcl(
            """
              hello = "you"
              """,
            """
              hello = "jonathan"
              """
          )
        );
    }
}
