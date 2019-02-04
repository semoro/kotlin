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

package org.jetbrains.kotlin.nj2k.conversions

import org.jetbrains.kotlin.nj2k.ConversionContext
import org.jetbrains.kotlin.j2k.ast.Nullability
import org.jetbrains.kotlin.nj2k.throwAnnotation
import org.jetbrains.kotlin.nj2k.tree.*
import org.jetbrains.kotlin.nj2k.tree.impl.JKKtFunctionImpl
import org.jetbrains.kotlin.nj2k.tree.impl.JKTypeElementImpl
import org.jetbrains.kotlin.nj2k.tree.impl.psi

class JavaMethodToKotlinFunctionConversion(private val context: ConversionContext) : TransformerBasedConversion() {
    override fun visitTreeElement(element: JKTreeElement) {
        element.acceptChildren(this, null)
    }

    override fun visitClassBody(classBody: JKClassBody) {
        somethingChanged = true

        classBody.declarations = classBody.declarations.map { declaration ->
            if (declaration is JKJavaMethod) {
                declaration.invalidate()

                JKKtFunctionImpl(
                    if (declaration.returnType.type.nullability != Nullability.NotNull)
                        JKTypeElementImpl(
                            declaration.returnType.type
                                .updateNullability(declaration.returnTypeNullability(context))
                        ) else declaration.returnType,
                    declaration.name,
                    declaration.parameters,
                    declaration.block,
                    declaration.typeParameterList,
                    declaration.annotationList.also {
                        if (declaration.throwsList.isNotEmpty()) {
                            it.annotations +=
                                    throwAnnotation(
                                        declaration.throwsList.map { it.type.updateNullabilityRecursively(Nullability.NotNull) },
                                        context.symbolProvider
                                    )
                        }
                    },
                    declaration.extraModifiers,
                    declaration.visibility,
                    declaration.modality
                ).also {
                    it.psi = declaration.psi
                }
            } else {
                declaration
            }
        }
        classBody.acceptChildren(this)
    }
}