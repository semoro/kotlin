/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import org.jetbrains.kotlin.j2k.tree.*
import org.jetbrains.kotlin.j2k.tree.impl.JKUniverseMethodSymbol
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

class DefaultArgumentsConversion : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        // TODO: Declaration list owner
        if (element !is JKClass) return recurse(element)

        val methods = element.declarationList.filterIsInstance<JKMethod>().sortedBy { it.parameters.size }

        checkMethod@ for (method in methods) {
            val block = method.block as? JKBlock ?: continue
            val singleStatement = block.statements.singleOrNull()

            val call =
                singleStatement.safeAs<JKExpressionStatement>()?.expression?.safeAs<JKMethodCallExpression>()
                        ?: singleStatement.safeAs<JKReturnStatement>()?.expression?.safeAs()
                        ?: continue
            val callee = call.identifier as? JKUniverseMethodSymbol ?: continue
            val calledMethod = callee.target
            if (calledMethod.parent != method.parent || callee.name != method.name.value) continue
            if (calledMethod.returnType.type != method.returnType.type) continue
            if (call.arguments.expressions.size <= method.parameters.size) continue


            // TODO: Filter by annotations, visibility, modality, modifiers like synchronized

            for (i in method.parameters.indices) {
                val parameter = method.parameters[i]
                val targetParameter = calledMethod.parameters[i]
                val argument = call.arguments.expressions[i]
                if (parameter.name.value != targetParameter.name.value) continue@checkMethod
                if (parameter.type.type != targetParameter.type.type) continue@checkMethod
                if (argument !is JKFieldAccessExpression || argument.identifier.target != parameter) continue@checkMethod
            }


            call.arguments.invalidate()
            val defaults = call.arguments.expressions
                .zip(calledMethod.parameters)
                .drop(method.parameters.size)

            for ((defaultValue, parameter) in defaults) {
                parameter.initializer = defaultValue
            }

            element.declarationList -= method


        }

        return recurse(element)

    }


}
