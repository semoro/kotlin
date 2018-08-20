/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.maven;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("idea/idea-maven/testData/maven-inspections")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class KotlinMavenInspectionTestGenerated extends AbstractKotlinMavenInspectionTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, TargetBackend.ANY, testDataFilePath);
    }

    public void testAllFilesPresentInMaven_inspections() throws Exception {
        KotlinTestUtils.assertAllTestsPresentInSingleGeneratedClass(this.getClass(), new File("idea/idea-maven/testData/maven-inspections"), Pattern.compile("^([\\w\\-]+).xml$"), TargetBackend.ANY);
    }

    @TestMetadata("bothCompileAndTestCompileInTheSameExecution.xml")
    public void testBothCompileAndTestCompileInTheSameExecution() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/bothCompileAndTestCompileInTheSameExecution.xml");
    }

    @TestMetadata("dependencyWithNoExecution.xml")
    public void testDependencyWithNoExecution() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/dependencyWithNoExecution.xml");
    }

    @TestMetadata("deprecatedJre.xml")
    public void testDeprecatedJre() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/deprecatedJre.xml");
    }

    @TestMetadata("deprecatedJreWithDependencyManagement.xml")
    public void testDeprecatedJreWithDependencyManagement() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/deprecatedJreWithDependencyManagement.xml");
    }

    @TestMetadata("deprecatedKotlinxCoroutines.xml")
    public void testDeprecatedKotlinxCoroutines() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/deprecatedKotlinxCoroutines.xml");
    }

    @TestMetadata("deprecatedKotlinxCoroutinesNoError.xml")
    public void testDeprecatedKotlinxCoroutinesNoError() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/deprecatedKotlinxCoroutinesNoError.xml");
    }

    @TestMetadata("ideAndMavenVersions.xml")
    public void testIdeAndMavenVersions() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/ideAndMavenVersions.xml");
    }

    @TestMetadata("ideAndMavenVersionsSuppression.xml")
    public void testIdeAndMavenVersionsSuppression() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/ideAndMavenVersionsSuppression.xml");
    }

    @TestMetadata("kotlinTestWithJunit.xml")
    public void testKotlinTestWithJunit() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/kotlinTestWithJunit.xml");
    }

    @TestMetadata("missingDependencies.xml")
    public void testMissingDependencies() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/missingDependencies.xml");
    }

    @TestMetadata("noExecutions.xml")
    public void testNoExecutions() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/noExecutions.xml");
    }

    @TestMetadata("oldVersionWithJre.xml")
    public void testOldVersionWithJre() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/oldVersionWithJre.xml");
    }

    @TestMetadata("sameVersionPluginLibrary.xml")
    public void testSameVersionPluginLibrary() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/sameVersionPluginLibrary.xml");
    }

    @TestMetadata("sameVersionPluginLibrarySuppression.xml")
    public void testSameVersionPluginLibrarySuppression() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/sameVersionPluginLibrarySuppression.xml");
    }

    @TestMetadata("wrongJsExecution.xml")
    public void testWrongJsExecution() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/wrongJsExecution.xml");
    }

    @TestMetadata("wrongPhaseExecution.xml")
    public void testWrongPhaseExecution() throws Exception {
        runTest("idea/idea-maven/testData/maven-inspections/wrongPhaseExecution.xml");
    }
}
