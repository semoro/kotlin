/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.nj2k.conversions

import org.jetbrains.kotlin.nj2k.ConversionContext
import org.jetbrains.kotlin.nj2k.tree.*
import org.jetbrains.kotlin.nj2k.tree.impl.JKKtInitDeclarationImpl
import org.jetbrains.kotlin.nj2k.tree.impl.JKKtPrimaryConstructorImpl

class PrimaryConstructorDetectConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element is JKClass &&
            (element.classKind == JKClass.ClassKind.CLASS || element.classKind == JKClass.ClassKind.ENUM)
        ) {
            processClass(element)
        }
        return recurse(element)
    }

    private fun <T> List<T>.replace(element: T, replacer: T): List<T> {
        val mutableList = toMutableList()
        val index = indexOf(element)
        mutableList[index] = replacer
        return mutableList
    }

    private fun processClass(element: JKClass) {
        val constructors = element.declarationList.filterIsInstance<JKKtConstructor>()
        if (constructors.any { it is JKKtPrimaryConstructor }) return
        val primaryConstructorCandidate = detectPrimaryConstructor(constructors) ?: return
        val delegationCall = primaryConstructorCandidate.delegationCall as? JKDelegationConstructorCall
        if (delegationCall?.expression is JKThisExpression) return


        primaryConstructorCandidate.invalidate()
        if (primaryConstructorCandidate.block.statements.isNotEmpty()) {
            val initDeclaration = JKKtInitDeclarationImpl(primaryConstructorCandidate.block)
            element.classBody.declarations =
                    element.classBody.declarations.replace(primaryConstructorCandidate, initDeclaration)
        } else {
            element.classBody.declarations -= primaryConstructorCandidate
        }

        val primaryConstructor =
            JKKtPrimaryConstructorImpl(
                primaryConstructorCandidate.name,
                primaryConstructorCandidate.parameters,
                primaryConstructorCandidate.delegationCall,
                primaryConstructorCandidate.annotationList,
                primaryConstructorCandidate.extraModifiers,
                primaryConstructorCandidate.visibility,
                primaryConstructorCandidate.modality
            )

        context.symbolProvider.transferSymbol(primaryConstructor, primaryConstructorCandidate)

        element.classBody.declarations += primaryConstructor
    }

    private fun detectPrimaryConstructor(constructors: List<JKKtConstructor>): JKKtConstructor? {
        val constructorsWithoutOtherConstructorCall =
            constructors.filterNot { (it.delegationCall as? JKDelegationConstructorCall)?.expression is JKThisExpression }
        return constructorsWithoutOtherConstructorCall.singleOrNull()
    }
}