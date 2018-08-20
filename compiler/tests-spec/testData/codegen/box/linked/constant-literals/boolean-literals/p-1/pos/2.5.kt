// HELPERS: REFLECT

/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SECTIONS: constant-literals, boolean-literals
 * PARAGRAPH: 1
 * SENTENCE: [2] These are strong keywords which cannot be used as identifiers unless escaped.
 * NUMBER: 5
 * DESCRIPTION: The use of Boolean literals as the identifier (with backtick) in the packageComplex.
 * NOTE: this test data is generated by FeatureInteractionTestDataGenerator. DO NOT MODIFY CODE MANUALLY!
 */

package org.jetbrains.`true`

fun box(): String? {
    if (!checkPackageName("org.jetbrains.true._2_5Kt", "org.jetbrains.true")) return null

    return "OK"
}
