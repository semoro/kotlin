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

package org.jetbrains.kotlin.j2k.tree.impl

import org.jetbrains.kotlin.j2k.tree.*

class JKKtPropertyImpl(override var modifierList: JKModifierList,
                       override var type: JKTypeIdentifier,
                       override var name: JKNameIdentifier,
                       override var initializer: JKExpression? = null,
                       override var getter: JKBlock? = null,
                       override var setter: JKBlock? = null) : JKElementBase(), JKKtProperty {


}

class JKKtFunctionImpl(override var returnType: JKTypeIdentifier,
                       override var name: JKNameIdentifier,
                       override var valueArguments: List<JKValueArgument>,
                       override var block: JKBlock?,
                       override var modifierList: JKModifierList) : JKElementBase(), JKKtFunction {

}
