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
package org.openrewrite.gradle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openrewrite.DocumentExample;
import org.openrewrite.Issue;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.buildGradle;
import static org.openrewrite.gradle.Assertions.buildGradleKts;

@SuppressWarnings("GroovyUnusedAssignment")
class UpdateJavaCompatibilityTest implements RewriteTest {

    @DocumentExample
    @Test
    void sourceOnly() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, UpdateJavaCompatibility.CompatibilityType.source, null, null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = 1.8
              targetCompatibility = 1.8
              """,
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = 11
              targetCompatibility = 1.8
              """
          )
        );
    }

    @CsvSource(textBlock = """
      1.8,1.8,11,11
      '1.8','1.8','11','11'
      "1.8","1.8","11","11"
      JavaVersion.VERSION_1_8,JavaVersion.VERSION_1_8,JavaVersion.VERSION_11,JavaVersion.VERSION_11
      1.8,"1.8",11,"11"
      JavaVersion.VERSION_1_8,"1.8",JavaVersion.VERSION_11,"11",
      """, quoteCharacter = '`')
    @ParameterizedTest
    void sourceAndTarget(String beforeSourceCompatibility, String beforeTargetCompatibility, String afterSourceCompatibility, String afterTargetCompatibility) {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = %s
              targetCompatibility = %s
              """.formatted(beforeSourceCompatibility, beforeTargetCompatibility),
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = %s
              targetCompatibility = %s
              """.formatted(afterSourceCompatibility, afterTargetCompatibility)
          )
        );
    }

    @Test
    void targetOnly() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, UpdateJavaCompatibility.CompatibilityType.target, null, null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = 1.8
              targetCompatibility = 1.8
              """,
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = 1.8
              targetCompatibility = 11
              """
          )
        );
    }

    @CsvSource(textBlock = """
      Enum,1.8,JavaVersion.VERSION_1_8
      Enum,'1.8',JavaVersion.VERSION_1_8
      Enum,"1.8",JavaVersion.VERSION_1_8
      Enum,JavaVersion.toVersion("1.8"),JavaVersion.VERSION_1_8
      Number,'1.8',1.8
      Number,"1.8",1.8
      Number,JavaVersion.VERSION_1_8,1.8
      Number,JavaVersion.toVersion("1.8"),1.8
      String,1.8,'1.8'
      String,JavaVersion.VERSION_1_8,'1.8'
      String,JavaVersion.toVersion("1.8"),'1.8'
      """, quoteCharacter = '`')
    @ParameterizedTest
    void styleChange(String declarationStyle, String beforeCompatibility, String afterCompatibility) {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(8, null, UpdateJavaCompatibility.DeclarationStyle.valueOf(declarationStyle), null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = %s
              targetCompatibility = %s
              """.formatted(beforeCompatibility, beforeCompatibility),
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = %s
              targetCompatibility = %s
              """.formatted(afterCompatibility, afterCompatibility)
          )
        );
    }

    @Test
    void handlesJavaExtension() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              java {
                  sourceCompatibility = 1.8
                  targetCompatibility = 1.8
              }
              """,
            """
              plugins {
                  id "java"
              }

              java {
                  sourceCompatibility = 11
                  targetCompatibility = 11
              }
              """
          )
        );
    }

    @CsvSource(textBlock = """
      1.8,11
      '1.8','11'
      """, quoteCharacter = '`')
    @ParameterizedTest
    void handlesJavaToolchains(String beforeCompatibility, String afterCompatibility) {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              java {
                  toolchain {
                      languageVersion = JavaLanguageVersion.of(%s)
                  }
              }
              """.formatted(beforeCompatibility),
            """
              plugins {
                  id "java"
              }

              java {
                  toolchain {
                      languageVersion = JavaLanguageVersion.of(%s)
                  }
              }
              """.formatted(afterCompatibility)
          )
        );
    }

    @CsvSource(textBlock = """
      11,"1.8","11"
      11,1.8,11
      11,8,11
      """)
    @Issue("https://github.com/openrewrite/rewrite/issues/3255")
    @ParameterizedTest
    void handlesJavaVersionMethodInvocation(int version, String before, String after) {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(version, null, null, null, null)),
          buildGradle(
            """
              java {
                  sourceCompatibility = JavaVersion.toVersion(%s)
                  targetCompatibility = JavaVersion.toVersion(%s)
              }
              """.formatted(before, before),
            """
              java {
                  sourceCompatibility = JavaVersion.toVersion(%s)
                  targetCompatibility = JavaVersion.toVersion(%s)
              }
              """.formatted(after, after)
          )
        );
    }

    @CsvSource(textBlock = """
      source,Enum,JavaVersion.VERSION_11,1.8
      source,Number,11,1.8
      source,String,'11',1.8
      target,Enum,1.8,JavaVersion.VERSION_11
      target,Number,1.8,11
      target,String,1.8,'11'
      """, quoteCharacter = '`')
    @ParameterizedTest
    void allOptions(String compatibilityType, String declarationStyle, String expectedSourceCompatibility, String expectedTargetCompatibility) {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, UpdateJavaCompatibility.CompatibilityType.valueOf(compatibilityType), UpdateJavaCompatibility.DeclarationStyle.valueOf(declarationStyle), null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = 1.8
              targetCompatibility = 1.8
              """,
            """
              plugins {
                  id "java"
              }

              sourceCompatibility = %s
              targetCompatibility = %s
              """.formatted(expectedSourceCompatibility, expectedTargetCompatibility)
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite/issues/3258")
    @Test
    void onlyModifyCompatibilityAssignments() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradle(
            """
              version = "0.1.0-SNAPSHOT"
              group = "com.example"
              java {
                  sourceCompatibility = JavaVersion.toVersion("1.8")
                  targetCompatibility = JavaVersion.toVersion("1.8")
              }
              """,
            """
              version = "0.1.0-SNAPSHOT"
              group = "com.example"
              java {
                  sourceCompatibility = JavaVersion.toVersion("11")
                  targetCompatibility = JavaVersion.toVersion("11")
              }
              """
          )
        );
    }

    @Test
    void doNotDowngradeByDefault() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(17, null, null, null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              java {
                  sourceCompatibility = 21
                  targetCompatibility = 21
                  toolchain {
                      languageVersion = JavaLanguageVersion.of(21)
                  }
              }
              """
          )
        );
    }

    @Test
    void doDowngradeWhenRequested() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(17, null, null, true, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              java {
                  sourceCompatibility = 21
                  targetCompatibility = 21
                  toolchain {
                      languageVersion = JavaLanguageVersion.of(21)
                  }
              }
              """,

            """
              plugins {
                  id "java"
              }

              java {
                  sourceCompatibility = 17
                  targetCompatibility = 17
                  toolchain {
                      languageVersion = JavaLanguageVersion.of(17)
                  }
              }
              """
          )
        );
    }

    @CsvSource(textBlock = """
      8,Enum,JavaVersion.VERSION_1_8,JavaVersion.VERSION_1_8
      8,Number,1.8,1.8
      8,String,'1.8','1.8'
      11,Enum,JavaVersion.VERSION_11,JavaVersion.VERSION_11
      11,Number,11,11
      11,String,'11','11'
      """, quoteCharacter = '`')
    @ParameterizedTest
    void addSourceAndTargetCompatibilityIfMissing(String version, String declarationStyle, String sourceCompatibility, String targetCompatibility) {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(Integer.valueOf(version), null, UpdateJavaCompatibility.DeclarationStyle.valueOf(declarationStyle), null, true)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              """,
            """
              plugins {
                  id "java"
              }
              sourceCompatibility = %s
              targetCompatibility = %s

              """.formatted(sourceCompatibility, targetCompatibility)
          )
        );
    }

    @Test
    void addSourceCompatibilityIfMissingAndRequested() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, UpdateJavaCompatibility.CompatibilityType.source, null, null, true)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              """,
            """
              plugins {
                  id "java"
              }
              sourceCompatibility = 11

              """
          )
        );
    }

    @Test
    void addTargetCompatibilityIfMissingAndRequested() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, UpdateJavaCompatibility.CompatibilityType.target, null, null, true)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              """,
            """
              plugins {
                  id "java"
              }
              targetCompatibility = 11

              """
          )
        );
    }

    @Issue("https://docs.gradle.org/current/userguide/building_java_projects.html#sec:compiling_with_release")
    @Test
    void releaseValueGetsUpdated() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradle(
            """
              plugins {
                  id "java"
              }

              tasks.withType(JavaCompile) {
                  options.release = 8
              }

              tasks.withType(JavaCompile).configureEach {
                  options.release = 8
              }

              compileJava {
                  options.release = 8
              }

              compileJava.options.release = 8
              """,
            """
              plugins {
                  id "java"
              }

              tasks.withType(JavaCompile) {
                  options.release = 11
              }

              tasks.withType(JavaCompile).configureEach {
                  options.release = 11
              }

              compileJava {
                  options.release = 11
              }

              compileJava.options.release = 11
              """
          )
        );
    }

    @Test
    void updateExisitingSourceCompatibilityInKotlinDSL() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, UpdateJavaCompatibility.CompatibilityType.source, null, null, null)),
          buildGradleKts(
            """
              plugins {
                  java
              }

              sourceCompatibility = JavaVersion.VERSION_1_8
              targetCompatibility = JavaVersion.VERSION_1_8
              """,
            """
              plugins {
                  java
              }

              sourceCompatibility = JavaVersion.VERSION_11
              targetCompatibility = JavaVersion.VERSION_1_8
              """
          )
        );
    }

    @Test
    void addCompatibilityInKotlinDSL() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, true)),
          buildGradleKts(
            """
              plugins {
                  java
              }
              """,
            """
              plugins {
                  java
              }

              java {
                  sourceCompatibility = JavaVersion.VERSION_11
                  targetCompatibility = JavaVersion.VERSION_11
              }
              """
          )
        );
    }

    @Test
    void handlesJavaToolchainsInKotlinDSL() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradleKts(
            """
              plugins {
                  java
              }

              java {
                  toolchain {
                      languageVersion.set(JavaLanguageVersion.of(8))
                  }
              }
              """,
            """
              plugins {
                  java
              }

              java {
                  toolchain {
                      languageVersion.set(JavaLanguageVersion.of(11))
                  }
              }
              """
          )
        );
    }

    @Test
    void handlesKotlinJvmToolchainInKotlinDSL() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradleKts(
            """
              kotlin {
                  jvmToolchain {
                      languageVersion.set(JavaLanguageVersion.of(8))
                  }
              }
              """,
            """
              kotlin {
                  jvmToolchain {
                      languageVersion.set(JavaLanguageVersion.of(11))
                  }
              }
              """
          )
        );
    }


    @Test
    void handlesKotlinJvmToolchainShorthandInKotlinDSL() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradleKts(
            """
              kotlin {
                  jvmToolchain(8)
              }
              """,
            """
              kotlin {
                  jvmToolchain(11)
              }
              """
          )
        );
    }

    @Test
    void toVersionInKotlinDSL() {
        rewriteRun(
          spec -> spec.recipe(new UpdateJavaCompatibility(11, null, null, null, null)),
          buildGradleKts(
            """
              version = "0.1.0-SNAPSHOT"
              group = "com.example"
              java {
                  sourceCompatibility = JavaVersion.toVersion("1.8")
                  targetCompatibility = JavaVersion.toVersion("1.8")
              }
              """,
            """
              version = "0.1.0-SNAPSHOT"
              group = "com.example"
              java {
                  sourceCompatibility = JavaVersion.toVersion("11")
                  targetCompatibility = JavaVersion.toVersion("11")
              }
              """
          )
        );
    }
}
