/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.nj2k.conversions

import org.jetbrains.kotlin.nj2k.ConversionContext
import org.jetbrains.kotlin.nj2k.tree.*
import org.jetbrains.kotlin.nj2k.tree.impl.*
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

class ClassToObjectPromotionConversion(private val context: ConversionContext) : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element is JKClass && element.classKind == JKClass.ClassKind.CLASS) {
            val companion =
                element.declarationList.firstIsInstanceOrNull<JKClass>()
                    ?.takeIf { it.classKind == JKClass.ClassKind.COMPANION }
                    ?: return recurse(element)

            val allDeclarationsMatches = element.declarationList.all {
                when (it) {
                    is JKKtPrimaryConstructor -> it.parameters.isEmpty() && it.block.statements.isEmpty()
                    is JKClass -> it.classKind == JKClass.ClassKind.COMPANION
                    else -> false
                }
            }

            if (allDeclarationsMatches && !element.hasInheritors()) {
                companion.invalidate()
                element.invalidate()
                return recurse(
                    JKClassImpl(
                        element.name,
                        element.inheritance,
                        JKClass.ClassKind.OBJECT,
                        element.typeParameterList,
                        companion.classBody.also {
                            it.handleDeclarationsModifiers()
                        },
                        JKAnnotationListImpl(),
                        element.extraModifiers,
                        element.visibility,
                        Modality.FINAL
                    )
                )
            }
        }

        return recurse(element)
    }

    private fun JKClassBody.handleDeclarationsModifiers() {
        for (declaration in declarations) {
            if (declaration !is JKVisibilityOwner) continue
            if (declaration.visibility == Visibility.PROTECTED) {
                //in old j2k it is internal. should it be private instead?
                declaration.visibility = Visibility.INTERNAL
            }
        }
    }

    private fun JKClass.hasInheritors() =
        context.converter.converterServices.oldServices.referenceSearcher.hasInheritors(psi()!!)
}