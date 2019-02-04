/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.j2k.ConversionContext
import org.jetbrains.kotlin.j2k.tree.JKBlock
import org.jetbrains.kotlin.j2k.tree.JKBlockStatement
import org.jetbrains.kotlin.j2k.tree.JKDeclaration
import org.jetbrains.kotlin.j2k.tree.JKTreeElement
import org.jetbrains.kotlin.j2k.tree.impl.*
import org.jetbrains.kotlin.name.ClassId

class BlockToRunConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKBlockStatement) return recurse(element)

        if (element.parent !is JKBlock) return recurse(element)

        val parentDeclaration = element.parentOfType<JKDeclaration>() ?: return recurse(element)
        val psiContext = parentDeclaration.psi ?: return recurse(element)


        element.invalidate()
        val lambda = JKLambdaExpressionImpl(
            JKBlockStatementImpl(element.block),
            emptyList()
        )
        val call = JKKtCallExpressionImpl(context.symbolProvider.provideByFqName("kotlin.run", true), JKExpressionListImpl(listOf(lambda)))
        return recurse(JKExpressionStatementImpl(call))
    }

}