/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.nj2k.conversions

import com.intellij.psi.CommonClassNames
import org.jetbrains.kotlin.nj2k.tree.*
import org.jetbrains.kotlin.nj2k.tree.impl.JKImportStatementImpl
import org.jetbrains.kotlin.nj2k.tree.impl.JKNameIdentifierImpl
import org.jetbrains.kotlin.nj2k.tree.visitors.JKVisitorVoid


class ImportStatementConversion : RecursiveApplicableConversionBase() {
    override fun applyToElement(element: JKTreeElement): JKTreeElement {
        if (element !is JKFile) return recurse(element)
        for (import in element.declarationList.collectImports()) {
            if (!element.importList.containsImport(import)) {
                element.importList += JKImportStatementImpl(JKNameIdentifierImpl(import))
            }
        }
        element.importList = element.importList.filter { it.name.value !in importExceptionList }
        return recurse(element)
    }

    private val importExceptionList =
        listOf(
            CommonClassNames.JAVA_UTIL_ARRAY_LIST,
            CommonClassNames.JAVA_UTIL_LIST,
            CommonClassNames.JAVA_UTIL_HASH_SET,
            CommonClassNames.JAVA_UTIL_HASH_MAP,
            CommonClassNames.JAVA_UTIL_COLLECTION,
            CommonClassNames.JAVA_UTIL_ITERATOR,
            "java.util.LinkedHashMap",
            "java.util.LinkedHashSet"
        )

    private fun importIsInPackage(import: String, packageName: String) =
        '.' !in import.substringAfter(packageName)


    private fun List<JKImportStatement>.containsImport(import: String) =
        asSequence()
            .map { it.name.value }
            .any {
                it == import ||
                        it.endsWith("*") && import.substringBeforeLast(".") == it.substringBeforeLast(".*")
            }

    private fun List<JKDeclaration>.collectImports(): List<String> {
        val collectImportsVisitor = CollectImportsVisitor()
        forEach {
            it.accept(collectImportsVisitor)
        }
        return collectImportsVisitor.collectedFqNames
    }


    private class CollectImportsVisitor : JKVisitorVoid {
        private val unfilteredCollectedFqNames = mutableSetOf<String>()

        private val defaultImports =
            listOf(
                "kotlin",
                "kotlin.annotation",
                "kotlin.collections",
                "kotlin.comparisons",
                "kotlin.io",
                "kotlin.ranges",
                "kotlin.sequences",
                "kotlin.text",
                "java.lang",
                "kotlin.jvm"
            )

        val collectedFqNames
            get() = unfilteredCollectedFqNames.filter {
                it.substringBeforeLast(".") !in defaultImports && it.contains(".")
            }

        override fun visitTreeElement(treeElement: JKTreeElement) {
            treeElement.acceptChildren(this)
        }

        override fun visitJavaNewExpression(javaNewExpression: JKJavaNewExpression) {
            unfilteredCollectedFqNames += javaNewExpression.classSymbol.fqName!!
            javaNewExpression.acceptChildren(this)
        }

        override fun visitTypeElement(typeElement: JKTypeElement, data: Nothing?) {
            val classType = typeElement.type as? JKClassType ?: return
            unfilteredCollectedFqNames += classType.classReference.fqName!!
        }

        override fun visitFieldAccessExpression(fieldAccessExpression: JKFieldAccessExpression) {
            unfilteredCollectedFqNames += fieldAccessExpression.identifier.fqName
        }

        override fun visitMethodCallExpression(methodCallExpression: JKMethodCallExpression) {
            unfilteredCollectedFqNames += methodCallExpression.identifier.fqName
        }

        override fun visitClassLiteralExpression(classLiteralExpression: JKClassLiteralExpression) {
            val type = classLiteralExpression.classType.type
            if (type is JKClassType) {
                unfilteredCollectedFqNames += type.classReference.fqName!!
            }
            classLiteralExpression.acceptChildren(this)
        }
    }
}