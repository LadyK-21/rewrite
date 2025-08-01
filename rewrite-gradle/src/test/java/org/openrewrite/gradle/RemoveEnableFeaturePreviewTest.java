/*
 * Copyright 2024 the original author or authors.
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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.gradle.Assertions.settingsGradle;

class RemoveEnableFeaturePreviewTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new RemoveEnableFeaturePreview("ONE_LOCKFILE_PER_PROJECT"));
    }

    @DocumentExample
    @Test
    void singleQuotes() {
        //language=gradle
        rewriteRun(
          settingsGradle(
            """
              pluginManagement {
                  repositories {
                      gradlePluginPortal()
                  }
              }

              rootProject.name = 'merge-service'
              enableFeaturePreview('ONE_LOCKFILE_PER_PROJECT')
              """,
            """
              pluginManagement {
                  repositories {
                      gradlePluginPortal()
                  }
              }

              rootProject.name = 'merge-service'
              """
          )
        );
    }

    @Test
    void doubleQuotes() {
        //language=gradle
        rewriteRun(
          settingsGradle(
            """
              pluginManagement {
                  repositories {
                      gradlePluginPortal()
                  }
              }

              rootProject.name = 'merge-service'
              enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT")

              include 'service'
              """,
            """
              pluginManagement {
                  repositories {
                      gradlePluginPortal()
                  }
              }

              rootProject.name = 'merge-service'

              include 'service'
              """
          )
        );
    }

    @Nested
    class NoChange {
        @Test
        void differentFeature() {
            //language=gradle
            rewriteRun(
              settingsGradle(
                """
                  pluginManagement {
                      repositories {
                          gradlePluginPortal()
                      }
                  }

                  enableFeaturePreview("DIFFERENT_FEATURE")

                  rootProject.name = 'merge-service'

                  include 'service'
                  """
              )
            );
        }

        @Test
        void nullArgument() {
            //language=gradle
            rewriteRun(
              settingsGradle(
                """
                  pluginManagement {
                      repositories {
                          gradlePluginPortal()
                      }
                  }

                  enableFeaturePreview(null)

                  rootProject.name = 'merge-service'

                  include 'service'
                  """
              )
            );
        }
    }
}
