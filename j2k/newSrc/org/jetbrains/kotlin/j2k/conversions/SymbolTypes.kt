/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.j2k.conversions

import org.jetbrains.kotlin.j2k.tree.JKType
import org.jetbrains.kotlin.j2k.tree.impl.JKFieldSymbol
import org.jetbrains.kotlin.j2k.tree.impl.JKJavaPrimitiveTypeImpl
import org.jetbrains.kotlin.j2k.tree.impl.JKUniverseFieldSymbol

fun JKFieldSymbol.getType(): JKType {
    if (this is JKUniverseFieldSymbol) {
        return this.target.type.type
    } else {
        // TODO implement for Multiverse
    }
    return JKJavaPrimitiveTypeImpl.BOOLEAN // just to compile
}