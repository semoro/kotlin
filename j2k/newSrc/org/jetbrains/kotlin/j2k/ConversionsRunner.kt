/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.j2k

import org.jetbrains.kotlin.j2k.conversions.*
import org.jetbrains.kotlin.j2k.tree.JKTreeElement

object ConversionsRunner {

    private fun createConversions(context: ConversionContext) = listOf(
        ModalityConversion(context),
        ImplicitInitializerConversion(),
        ImplicitTypeCastConversion(context),
        TypeMappingConversion(context),
        FieldToPropertyConversion(),
        AssignmentAsExpressionToAlsoConversion(context),
        AssignmentStatenmentValCreationConversion(context),
        AssignmentStatenmentOperatorConversion(),
        ConstructorConversion(context),
        PrimaryConstructorDetectConversion(context),
        JavaMethodToKotlinFunctionConversion(),
        LiteralConversion(),
        InnerClassConversion(),
        ModifiersConversion(context),
        PolyadicExpressionConversion(),
        BinaryExpressionConversion()
    )

    fun doApply(trees: List<JKTreeElement>, context: ConversionContext) {
        val conversions = createConversions(context)
        trees.forEach { tree ->
            conversions.forEach { it.runConversion(tree, context) }
        }
    }

}