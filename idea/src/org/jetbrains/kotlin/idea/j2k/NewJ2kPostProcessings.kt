/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.idea.j2k

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.codeInspection.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.parentOfType
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.idea.analysis.analyzeInContext
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.core.replaced
import org.jetbrains.kotlin.idea.core.setVisibility
import org.jetbrains.kotlin.idea.inspections.*
import org.jetbrains.kotlin.idea.inspections.branchedTransformations.IfThenToSafeAccessInspection
import org.jetbrains.kotlin.idea.inspections.conventionNameCalls.ReplaceGetOrSetInspection
import org.jetbrains.kotlin.idea.intentions.*
import org.jetbrains.kotlin.idea.intentions.branchedTransformations.intentions.FoldIfToReturnAsymmetricallyIntention
import org.jetbrains.kotlin.idea.intentions.branchedTransformations.intentions.FoldIfToReturnIntention
import org.jetbrains.kotlin.idea.intentions.branchedTransformations.intentions.IfThenToElvisIntention
import org.jetbrains.kotlin.idea.intentions.branchedTransformations.isTrivialStatementBody
import org.jetbrains.kotlin.idea.j2k.postProcessing.ConvertDataClass
import org.jetbrains.kotlin.idea.j2k.postProcessing.ConvertGettersAndSetters
import org.jetbrains.kotlin.idea.j2k.postProcessing.resolve
import org.jetbrains.kotlin.idea.j2k.postProcessing.topLevelContainingClassOrObject
import org.jetbrains.kotlin.idea.quickfix.*
import org.jetbrains.kotlin.idea.references.KtSimpleNameReference
import org.jetbrains.kotlin.idea.references.mainReference
import org.jetbrains.kotlin.idea.references.readWriteAccess
import org.jetbrains.kotlin.idea.util.getResolutionScope
import org.jetbrains.kotlin.j2k.ConverterSettings
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode
import org.jetbrains.kotlin.types.isNullable
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlin.utils.mapToIndex
import java.util.*

object NewJ2KPostProcessingRegistrarImpl : J2KPostProcessingRegistrar {

    private fun Processing.processings(): Sequence<J2kPostProcessing> =
        when (this) {
            is SingleProcessing -> sequenceOf(processing)
            is ProcessingGroup -> processings.asSequence().flatMap { it.processings() }
            else -> sequenceOf()
        }


    private val processingsToPriorityMap = HashMap<J2kPostProcessing, Int>()

    override fun priority(processing: J2kPostProcessing): Int = processingsToPriorityMap[processing]!!

    override val mainProcessings = ProcessingGroup(
        SingleProcessing(VarToVal()),
        ProcessingGroup(
            SingleProcessing(ConvertGettersAndSetters()),
            registerGeneralInspectionBasedProcessing(RedundantModalityModifierInspection()),
            registerGeneralInspectionBasedProcessing(RedundantVisibilityModifierInspection()),
            registerGeneralInspectionBasedProcessing(RedundantGetterInspection()),
            registerGeneralInspectionBasedProcessing(RedundantSetterInspection())
        ),
        SingleProcessing(ConvertDataClass()),
        ProcessingGroup(
            registerGeneralInspectionBasedProcessing(ExplicitThisInspection()),

            SingleProcessing(RemoveExplicitTypeArgumentsProcessing()),
            SingleProcessing(RemoveRedundantOverrideVisibilityProcessing()),
            registerInspectionBasedProcessing(MoveLambdaOutsideParenthesesInspection()),
            registerGeneralInspectionBasedProcessing(RedundantCompanionReferenceInspection()),
            SingleProcessing(FixObjectStringConcatenationProcessing()),
            SingleProcessing(ConvertToStringTemplateProcessing()),
            SingleProcessing(UsePropertyAccessSyntaxProcessing()),
            SingleProcessing(UninitializedVariableReferenceFromInitializerToThisReferenceProcessing()),
            SingleProcessing(UnresolvedVariableReferenceFromInitializerToThisReferenceProcessing()),
            SingleProcessing(RemoveRedundantSamAdaptersProcessing()),
            SingleProcessing(RemoveRedundantCastToNullableProcessing()),
            registerInspectionBasedProcessing(ReplacePutWithAssignmentInspection()),
            SingleProcessing(UseExpressionBodyProcessing()),
            registerInspectionBasedProcessing(UnnecessaryVariableInspection()),
            SingleProcessing(
                object : J2kPostProcessing {
                    override val writeActionNeeded: Boolean = true
                    private val processing = registerGeneralInspectionBasedProcessing(RedundantExplicitTypeInspection())

                    override fun createAction(element: KtElement, diagnostics: Diagnostics, settings: ConverterSettings?): (() -> Unit)? {
                        if (settings?.specifyLocalVariableTypeByDefault == true) return null

                        return processing.processing.createAction(element, diagnostics)
                    }
                }
            ),
            registerGeneralInspectionBasedProcessing(RedundantUnitReturnTypeInspection()),

            SingleProcessing(RemoveExplicitPropertyType()),
            SingleProcessing(RemoveRedundantNullability()),

            registerGeneralInspectionBasedProcessing(CanBeValInspection(ignoreNotUsedVals = false)),

            registerIntentionBasedProcessing(FoldInitializerAndIfToElvisIntention()),
            registerGeneralInspectionBasedProcessing(RedundantSemicolonInspection()),
            registerIntentionBasedProcessing(RemoveEmptyClassBodyIntention()),
            registerIntentionBasedProcessing(RemoveRedundantCallsOfConversionMethodsIntention()),

            registerIntentionBasedProcessing(FoldIfToReturnIntention()) { it.then.isTrivialStatementBody() && it.`else`.isTrivialStatementBody() },
            registerIntentionBasedProcessing(FoldIfToReturnAsymmetricallyIntention()) {
                it.then.isTrivialStatementBody() && (KtPsiUtil.skipTrailingWhitespacesAndComments(
                    it
                ) as KtReturnExpression).returnedExpression.isTrivialStatementBody()
            },


            registerInspectionBasedProcessing(IfThenToSafeAccessInspection()),
            registerInspectionBasedProcessing(IfThenToSafeAccessInspection()),
            registerIntentionBasedProcessing(IfThenToElvisIntention()),
            registerInspectionBasedProcessing(SimplifyNegatedBinaryExpressionInspection()),
            registerInspectionBasedProcessing(ReplaceGetOrSetInspection()),
            registerIntentionBasedProcessing(AddOperatorModifierIntention()),
            registerIntentionBasedProcessing(ObjectLiteralToLambdaIntention()),
            registerIntentionBasedProcessing(AnonymousFunctionToLambdaIntention()),
            registerIntentionBasedProcessing(RemoveUnnecessaryParenthesesIntention()),
            registerIntentionBasedProcessing(DestructureIntention()),
            registerInspectionBasedProcessing(SimplifyAssertNotNullInspection()),
            registerIntentionBasedProcessing(RemoveRedundantCallsOfConversionMethodsIntention()),
            registerGeneralInspectionBasedProcessing(LiftReturnOrAssignmentInspection()),
            registerGeneralInspectionBasedProcessing(MayBeConstantInspection()),
            registerIntentionBasedProcessing(RemoveEmptyPrimaryConstructorIntention()),
            registerDiagnosticBasedProcessing(Errors.PLATFORM_CLASS_MAPPED_TO_KOTLIN) { element: KtDotQualifiedExpression, diagnostic ->
                val parent = element.parent as? KtImportDirective ?: return@registerDiagnosticBasedProcessing
                parent.delete()
            },

            registerDiagnosticBasedProcessing(Errors.CAST_NEVER_SUCCEEDS) { element: KtSimpleNameExpression, diagnostic ->
                val action =
                    ReplacePrimitiveCastWithNumberConversionFix.createActionsForAllProblems(listOf(diagnostic)).singleOrNull()
                        ?: return@registerDiagnosticBasedProcessing
                action.invoke(element.project, null, element.containingKtFile)
            },

            SingleProcessing(RemoveRedundantTypeQualifierProcessing()),
            SingleProcessing(RemoveRedundantExpressionQualifierProcessing()),

            registerDiagnosticBasedProcessing<KtBinaryExpressionWithTypeRHS>(Errors.USELESS_CAST) { element, _ ->
                val expression = RemoveUselessCastFix.invoke(element)

                val variable = expression.parent as? KtProperty
                if (variable != null && expression == variable.initializer && variable.isLocal) {
                    val ref = ReferencesSearch.search(variable, LocalSearchScope(variable.containingFile)).findAll().singleOrNull()
                    if (ref != null && ref.element is KtSimpleNameExpression) {
                        ref.element.replace(expression)
                        variable.delete()
                    }
                }
            },

            registerDiagnosticBasedProcessing<KtTypeProjection>(Errors.REDUNDANT_PROJECTION) { _, diagnostic ->
                val fix = RemoveModifierFix.createRemoveProjectionFactory(true).createActions(diagnostic).single() as RemoveModifierFix
                fix.invoke()
            },

            registerDiagnosticBasedProcessing<KtModifierListOwner>(Errors.VIRTUAL_MEMBER_HIDDEN) { element, diagnostic ->
                val action = AddModifierFix
                    .createFactory(KtTokens.OVERRIDE_KEYWORD)
                    .createActions(diagnostic)
                    .singleOrNull() ?: return@registerDiagnosticBasedProcessing
                action.invoke(element.project, null, element.containingKtFile)
            },

            registerDiagnosticBasedProcessing<KtModifierListOwner>(Errors.NON_FINAL_MEMBER_IN_FINAL_CLASS) { _, diagnostic ->
                val fix =
                    RemoveModifierFix
                        .createRemoveModifierFromListOwnerFactory(KtTokens.OPEN_KEYWORD)
                        .createActions(diagnostic).single() as RemoveModifierFix
                fix.invoke()
            },
            registerDiagnosticBasedProcessing<KtModifierListOwner>(Errors.NON_FINAL_MEMBER_IN_OBJECT) { _, diagnostic ->
                val fix =
                    RemoveModifierFix
                        .createRemoveModifierFromListOwnerFactory(KtTokens.OPEN_KEYWORD)
                        .createActions(diagnostic).single() as RemoveModifierFix
                fix.invoke()
            },

            registerDiagnosticBasedProcessingFactory(
                Errors.VAL_REASSIGNMENT, Errors.CAPTURED_VAL_INITIALIZATION, Errors.CAPTURED_MEMBER_VAL_INITIALIZATION
            ) { element: KtSimpleNameExpression, _: Diagnostic ->
                val property = element.mainReference.resolve() as? KtProperty
                if (property == null) {
                    null
                } else {
                    {
                        if (!property.isVar) {
                            property.valOrVarKeyword.replace(KtPsiFactory(element.project).createVarKeyword())
                        }
                    }
                }
            },

            registerDiagnosticBasedProcessing<KtSimpleNameExpression>(Errors.UNNECESSARY_NOT_NULL_ASSERTION) { element, _ ->
                val exclExclExpr = element.parent as KtUnaryExpression
                val baseExpression = exclExclExpr.baseExpression!!
                val context = baseExpression.analyze(BodyResolveMode.PARTIAL_WITH_DIAGNOSTICS)
                if (context.diagnostics.forElement(element).any { it.factory == Errors.UNNECESSARY_NOT_NULL_ASSERTION }) {
                    exclExclExpr.replace(baseExpression)
                }
            }
        )
    )

    override val processings: Collection<J2kPostProcessing> =
        mainProcessings.processings().toList()


    init {
        processingsToPriorityMap.putAll(processings.mapToIndex())
    }


    private inline fun <reified TElement : KtElement, TIntention : SelfTargetingRangeIntention<TElement>> registerIntentionBasedProcessing(
        intention: TIntention,
        noinline additionalChecker: (TElement) -> Boolean = { true }
    ) = SingleProcessing(object : J2kPostProcessing {
        // Intention can either need or not need write action
        override val writeActionNeeded = intention.startInWriteAction()

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (!TElement::class.java.isInstance(element)) return null
            val tElement = element as TElement
            if (intention.applicabilityRange(tElement) == null) return null
            if (!additionalChecker(tElement)) return null
            return {
                if (intention.applicabilityRange(tElement) != null) { // check availability of the intention again because something could change
                    intention.applyTo(element, null)
                }
            }
        }
    }
    )


    private inline fun <TInspection : AbstractKotlinInspection> registerGeneralInspectionBasedProcessing(
        inspection: TInspection,
        acceptInformationLevel: Boolean = false
    ) = SingleProcessing(object : J2kPostProcessing {
        override val writeActionNeeded = false

        fun <D : CommonProblemDescriptor> QuickFix<D>.applyFixSmart(project: Project, descriptor: D) {
            if (descriptor is ProblemDescriptor) {
                if (this is IntentionWrapper) {
                    @Suppress("NOT_YET_SUPPORTED_IN_INLINE")
                    fun applySelfTargetingIntention(action: SelfTargetingIntention<PsiElement>) {
                        val target = action.getTarget(descriptor.psiElement.startOffset, descriptor.psiElement.containingFile) ?: return
                        if (!action.isApplicableTo(target, descriptor.psiElement.startOffset)) return
                        action.applyTo(target, null)
                    }

                    @Suppress("NOT_YET_SUPPORTED_IN_INLINE")
                    fun applyQuickFixActionBase(action: QuickFixActionBase<PsiElement>) {
                        if (!action.isAvailable(project, null, descriptor.psiElement.containingFile)) return
                        action.invoke(project, null, descriptor.psiElement.containingFile)
                    }


                    @Suppress("NOT_YET_SUPPORTED_IN_INLINE")
                    fun applyIntention() {
                        val action = this.action
                        when (action) {
                            is SelfTargetingIntention<*> -> applySelfTargetingIntention(action as SelfTargetingIntention<PsiElement>)
                            is QuickFixActionBase<*> -> applyQuickFixActionBase(action)
                        }
                    }


                    if (this.startInWriteAction()) {
                        ApplicationManager.getApplication().runWriteAction(::applyIntention)
                    } else {
                        applyIntention()
                    }

                }
            }

            ApplicationManager.getApplication().runWriteAction {
                this.applyFix(project, descriptor)
            }
        }

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            val holder = ProblemsHolder(InspectionManager.getInstance(element.project), element.containingFile, false)
            val visitor = inspection.buildVisitor(
                holder,
                false,
                LocalInspectionToolSession(element.containingFile, 0, element.containingFile.endOffset)
            )
            element.accept(visitor)
            if (!holder.hasResults()) return null
            return {
                holder.results.clear()
                element.accept(visitor)
                if (holder.hasResults()) {
                    holder.results
                        .filter { acceptInformationLevel || it.highlightType != ProblemHighlightType.INFORMATION }
                        .forEach { it.fixes?.firstOrNull()?.applyFixSmart(element.project, it) }
                }
            }
        }
    })


    private inline fun
            <reified TElement : KtElement,
                    TInspection : AbstractApplicabilityBasedInspection<TElement>> registerInspectionBasedProcessing(

        inspection: TInspection,
        acceptInformationLevel: Boolean = false
    ) = SingleProcessing(object : J2kPostProcessing {
        // Inspection can either need or not need write action
        override val writeActionNeeded = inspection.startFixInWriteAction

        private fun isApplicable(element: TElement): Boolean {
            if (!inspection.isApplicable(element)) return false
            return acceptInformationLevel || inspection.inspectionHighlightType(element) != ProblemHighlightType.INFORMATION
        }

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (!TElement::class.java.isInstance(element)) return null
            val tElement = element as TElement
            if (!isApplicable(tElement)) return null
            return {
                if (isApplicable(tElement)) { // check availability of the inspection again because something could change
                    inspection.applyTo(inspection.inspectionTarget(tElement))
                }
            }
        }
    })


    private inline fun <reified TElement : KtElement> registerDiagnosticBasedProcessing(
        vararg diagnosticFactory: DiagnosticFactory<*>,
        crossinline fix: (TElement, Diagnostic) -> Unit
    ) = registerDiagnosticBasedProcessingFactory(*diagnosticFactory) { element: TElement, diagnostic: Diagnostic ->
        {
            fix(
                element,
                diagnostic
            )
        }
    }


    private inline fun <reified TElement : KtElement> registerDiagnosticBasedProcessingFactory(
        vararg diagnosticFactory: DiagnosticFactory<*>,
        crossinline fixFactory: (TElement, Diagnostic) -> (() -> Unit)?
    ) = SingleProcessing(object : J2kPostProcessing {
        // ???
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (!TElement::class.java.isInstance(element)) return null
            val diagnostic = diagnostics.forElement(element).firstOrNull { it.factory in diagnosticFactory } ?: return null
            return fixFactory(element as TElement, diagnostic)
        }
    })


    private class RemoveExplicitPropertyType : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics, settings: ConverterSettings?): (() -> Unit)? {
            if (element !is KtProperty) return null
            val needFieldTypes = settings?.specifyFieldTypeByDefault == true
            val needLocalVariablesTypes = settings?.specifyLocalVariableTypeByDefault == true

            fun check(element: KtProperty): Boolean {
                if (needLocalVariablesTypes && element.isLocal) return false
                if (needFieldTypes && element.isMember) return false
                val initializer = element.initializer ?: return false
                val withoutExpectedType = initializer.analyzeInContext(initializer.getResolutionScope())
                val descriptor = element.resolveToDescriptorIfAny() as? CallableDescriptor ?: return false
                return when (withoutExpectedType.getType(initializer)) {
                    descriptor.returnType -> true
                    descriptor.returnType?.makeNotNullable() -> !element.isVar
                    else -> false
                }
            }

            if (!check(element)) {
                return null
            } else {
                return {
                    if (element.isValid && check(element)) {
                        element.typeReference = null
                    }
                }
            }
        }
    }


    private class RemoveRedundantNullability : J2kPostProcessing {
        override val writeActionNeeded: Boolean = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtProperty) return null

            fun check(element: KtProperty): Boolean {
                if (!element.isLocal) return false
                val typeReference = element.typeReference
                if (typeReference == null || typeReference.typeElement !is KtNullableType) return false
                val initializerType = element.initializer?.let {
                    it.analyzeInContext(element.getResolutionScope()).getType(it)
                }
                if (initializerType?.isNullable() == true) return false

                return ReferencesSearch.search(element, element.useScope).findAll().mapNotNull { ref ->
                    val parent = (ref.element.parent as? KtExpression)?.asAssignment()
                    parent?.takeIf { it.left == ref.element }
                }.all {
                    val right = it.right
                    val withoutExpectedType = right?.analyzeInContext(element.getResolutionScope())
                    withoutExpectedType?.getType(right)?.isNullable() == false
                }
            }

            if (!check(element)) {
                return null
            } else {
                return {
                    val typeElement = element.typeReference?.typeElement
                    if (element.isValid && check(element) && typeElement != null && typeElement is KtNullableType) {
                        typeElement.replace(typeElement.innerType!!)
                    }
                }
            }
        }
    }

    private class RemoveExplicitTypeArgumentsProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtTypeArgumentList || !RemoveExplicitTypeArgumentsIntention.isApplicableTo(
                    element,
                    approximateFlexible = true
                )
            ) return null

            return {
                if (RemoveExplicitTypeArgumentsIntention.isApplicableTo(element, approximateFlexible = true)) {
                    element.delete()
                }
            }
        }
    }

    private class RemoveRedundantOverrideVisibilityProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtCallableDeclaration || !element.hasModifier(KtTokens.OVERRIDE_KEYWORD)) return null
            val modifier = element.visibilityModifierType() ?: return null
            return { element.setVisibility(modifier) }
        }
    }

    private class ConvertToStringTemplateProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        private val intention = ConvertToStringTemplateIntention()

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element is KtBinaryExpression && intention.isApplicableTo(element) && ConvertToStringTemplateIntention.shouldSuggestToConvert(
                    element
                )
            ) {
                return { intention.applyTo(element, null) }
            } else {
                return null
            }
        }
    }

    private class UsePropertyAccessSyntaxProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        private val intention = UsePropertyAccessSyntaxIntention()

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtCallExpression) return null
            val propertyName = intention.detectPropertyNameToUse(element) ?: return null
            return { intention.applyTo(element, propertyName, reformat = true) }
        }
    }

    private class RemoveRedundantSamAdaptersProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtCallExpression) return null

            val expressions = RedundantSamConstructorInspection.samConstructorCallsToBeConverted(element)
            if (expressions.isEmpty()) return null

            return {
                RedundantSamConstructorInspection.samConstructorCallsToBeConverted(element)
                    .forEach { RedundantSamConstructorInspection.replaceSamConstructorCall(it) }
            }
        }
    }

    private class UseExpressionBodyProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtPropertyAccessor) return null

            val inspection = UseExpressionBodyInspection(convertEmptyToUnit = false)
            if (!inspection.isActiveFor(element)) return null

            return {
                if (inspection.isActiveFor(element)) {
                    inspection.simplify(element, false)
                }
            }
        }
    }

    private class RemoveRedundantCastToNullableProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtBinaryExpressionWithTypeRHS) return null

            val context = element.analyze()
            val leftType = context.getType(element.left) ?: return null
            val rightType = context.get(BindingContext.TYPE, element.right) ?: return null

            if (!leftType.isMarkedNullable && rightType.isMarkedNullable) {
                return {
                    val type = element.right?.typeElement as? KtNullableType
                    type?.replace(type.innerType!!)
                }
            }

            return null
        }
    }

    private class FixObjectStringConcatenationProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtBinaryExpression ||
                element.operationToken != KtTokens.PLUS ||
                diagnostics.forElement(element.operationReference).none {
                    it.factory == Errors.UNRESOLVED_REFERENCE_WRONG_RECEIVER
                            || it.factory == Errors.NONE_APPLICABLE
                }
            )
                return null

            val bindingContext = element.analyze()
            val rightType = element.right?.getType(bindingContext) ?: return null

            if (KotlinBuiltIns.isString(rightType)) {
                return {
                    val factory = KtPsiFactory(element)
                    element.left!!.replace(factory.buildExpression {
                        appendFixedText("(")
                        appendExpression(element.left)
                        appendFixedText(").toString()")
                    })
                }
            }
            return null
        }
    }

    private class UninitializedVariableReferenceFromInitializerToThisReferenceProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtSimpleNameExpression || diagnostics.forElement(element).none { it.factory == Errors.UNINITIALIZED_VARIABLE }) return null

            val resolved = element.mainReference.resolve() ?: return null
            if (resolved.isAncestor(element, strict = true)) {
                if (resolved is KtVariableDeclaration && resolved.hasInitializer()) {
                    val anonymousObject = element.getParentOfType<KtClassOrObject>(true) ?: return null
                    if (resolved.initializer!!.getChildOfType<KtClassOrObject>() == anonymousObject) {
                        return { element.replaced(KtPsiFactory(element).createThisExpression()) }
                    }
                }
            }

            return null
        }
    }

    private class UnresolvedVariableReferenceFromInitializerToThisReferenceProcessing : J2kPostProcessing {
        override val writeActionNeeded = true

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtSimpleNameExpression || diagnostics.forElement(element).none { it.factory == Errors.UNRESOLVED_REFERENCE }) return null

            val anonymousObject = element.getParentOfType<KtClassOrObject>(true) ?: return null

            val variable = anonymousObject.getParentOfType<KtVariableDeclaration>(true) ?: return null

            if (variable.nameAsName == element.getReferencedNameAsName() &&
                variable.initializer?.getChildOfType<KtClassOrObject>() == anonymousObject
            ) {
                return { element.replaced(KtPsiFactory(element).createThisExpression()) }
            }

            return null
        }
    }

    private class VarToVal : J2kPostProcessing {
        override val writeActionNeeded = true

        private fun KtProperty.hasWriteUsages(): Boolean =
            ReferencesSearch.search(this, useScope).any { usage ->
                (usage as? KtSimpleNameReference)?.element?.let {
                    it.readWriteAccess(useResolveForReadWrite = true).isWrite
                            && it.parentOfType<KtAnonymousInitializer>() == null//TODO properly check
                } == true
            }

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtProperty) return null

            fun check(element: KtProperty): Boolean {
                if (!element.isVar) return false
                if (!element.isPrivate()) return false
                return !element.hasWriteUsages()
            }

            if (!check(element)) {
                return null
            } else {
                return {
                    if (element.isValid && check(element)) {
                        val factory = KtPsiFactory(element)
                        element.valOrVarKeyword.replace(factory.createValKeyword())
                        println()
                    }
                }
            }
        }
    }

    private class RemoveRedundantExpressionQualifierProcessing : J2kPostProcessing {
        private fun check(qualifiedExpression: KtQualifiedExpression): Boolean {
            val qualifier = (qualifiedExpression.receiverExpression as? KtNameReferenceExpression)
                ?.referenceExpression()
                ?.resolve() as? KtClassOrObject ?: return false
            val topLevelClass = qualifiedExpression.getStrictParentOfType<KtClassOrObject>() ?: return false
            return topLevelClass == qualifier
        }

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtQualifiedExpression) return null
            if (!check(element)) return null
            return {
                if (element.isValid() && check(element)) {
                    element.replace(element.selectorExpression!!)
                }
            }
        }

        override val writeActionNeeded: Boolean = true
    }

    private class RemoveRedundantTypeQualifierProcessing : J2kPostProcessing {
        private fun check(reference: KtUserType): Boolean {
            val qualifierClass = reference.qualifier
                ?.referenceExpression
                ?.resolve() as? KtClassOrObject ?: return false
            val topLevelClass = reference.topLevelContainingClassOrObject() ?: return false
            return topLevelClass.isAncestor(qualifierClass)
        }

        override fun createAction(element: KtElement, diagnostics: Diagnostics): (() -> Unit)? {
            if (element !is KtUserType) return null
            if (!check(element)) return null
            return {
                if (element.isValid && check(element)) {
                    element.deleteQualifier()
                }
            }
        }

        override val writeActionNeeded: Boolean = true
    }

}