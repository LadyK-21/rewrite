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
package org.openrewrite.internal

import org.junit.jupiter.api.Test
import org.openrewrite.Option
import org.openrewrite.Recipe

class RecipeIntrospectionUtilsTest {

    @Test
    fun kotlinNonNullConstructorArgs() {
        RecipeIntrospectionUtils.constructRecipe(TestRecipe::class.java)
    }

    enum class EnumOption {
        Value
    }

    class TestRecipe constructor(
        @Option val option1: String,
        @Option val option2: EnumOption,
        @Option val option3: List<String>,
        @Option val option4: Boolean
    ) : Recipe() {
        override fun getDisplayName() = "Test"
    }
}
