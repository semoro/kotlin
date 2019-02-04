/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.nj2k.conversions

import org.jetbrains.kotlin.nj2k.ConversionContext
import org.jetbrains.kotlin.j2k.ast.Nullability
import org.jetbrains.kotlin.nj2k.tree.*
import org.jetbrains.kotlin.nj2k.tree.impl.JKFieldSymbol
import org.jetbrains.kotlin.nj2k.tree.impl.JKTypeElementImpl

class LowerNullabilityInFunctionParametersConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKMethod) return recurse(element)
        val scopes =
            listOfNotNull(
                element.block,
                (element as? JKKtConstructor)?.delegationCall
            ) + element.parameters.mapNotNull {
                if (it.initializer !is JKStubExpression) it.initializer else null
            }
        for (parameter in element.parameters) {
            if (parameter.type.type.nullability != Nullability.Default) continue
            if (parameter.hasNotNullUsages(scopes)) {
                parameter.type = JKTypeElementImpl(parameter.type.type.updateNullability(Nullability.NotNull))
            }
        }
        return recurse(element)
    }

    private fun JKParameter.hasNotNullUsages(scopes: List<JKTreeElement>): Boolean =
        scopes.any {
            val searcher = HasNotNullUsagesSearcher(context.symbolProvider.provideUniverseSymbol(this))
            scopes.any { searcher.runConversion(it, context) }
            searcher.found
        }

    private inner class HasNotNullUsagesSearcher(private val parameterSymbol: JKFieldSymbol) : RecursiveApplicableConversionBase() {
        var found: Boolean = false
        override fun applyToElement(element: JKTreeElement): JKTreeElement {
            when (element) {
                is JKQualifiedExpression -> {
                    val receiver = element.receiver as? JKFieldAccessExpression ?: return recurse(element)
                    if (receiver.identifier == parameterSymbol) {
                        found = true
                        return element
                    }
                }
            }
            return recurse(element)
        }
    }

}