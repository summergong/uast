package org.jetbrains.uast.test.java

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UParameter
import org.jetbrains.uast.UVariable
import org.jetbrains.uast.evaluation.UEvaluationInfo
import org.jetbrains.uast.evaluation.UEvaluationState
import org.jetbrains.uast.evaluation.UEvaluatorExtension
import org.jetbrains.uast.values.UBooleanConstant
import org.jetbrains.uast.values.UStringConstant
import org.jetbrains.uast.values.UValue
import org.junit.Test

class JavaValuesTest : AbstractJavaValuesTest() {
    @Test fun testAliveThenElse() = doTest("Simple/AliveThenElse.java")

    @Test fun testAnonymous() = doTest("Simple/Anonymous.java")

    @Test fun testBitwise() = doTest("Simple/Bitwise.java")

    @Test fun testByteShort() = doTest("Simple/ByteShort.java")

    @Test fun testCascadeIf() = doTest("Simple/CascadeIf.java")

    @Test fun testCharacters() = doTest("Simple/Characters.java")

    @Test fun testClassLiteral() = doTest("Simple/ClassLiteral.java")

    @Test fun testDeadElse() = doTest("Simple/DeadElse.java")

    @Test fun testDeadFor() = doTest("Simple/DeadFor.java")

    @Test fun testDeadIfComparison() = doTest("Simple/DeadIfComparison.java")

    @Test fun testDeadSwitchEntries() = doTest("Simple/DeadSwitchEntries.java")

    @Test fun testDeadSwitchEntriesWithoutBreaks() = doTest("Simple/DeadSwitchEntriesWithoutBreaks.java")

    @Test fun testDeadThen() = doTest("Simple/DeadThen.java")

    @Test fun testDependents() = doTest("Simple/Dependents.java")

    @Test fun testDoWhile() = doTest("Simple/DoWhile.java")

    @Test fun testDoWhileInfinite() = doTest("Simple/DoWhileInfinite.java")

    @Test fun testDoWhileWithReturn() = doTest("Simple/DoWhileWithReturn.java")

    @Test fun testEnumChoice() = doTest("Simple/EnumChoice.java")

    @Test fun testEnumSwitch() = doTest("Simple/EnumSwitch.java")

    @Test fun testEnumSwitchConditionalBreak() = doTest("Simple/EnumSwitchConditionalBreak.java")

    @Test fun testEnumSwitchWithoutBreaks() = doTest("Simple/EnumSwitchWithoutBreaks.java")

    @Test fun testExternal() = doTest("Simple/External.java")

    @Test fun testFieldRef() = doTest("Simple/FieldRef.java")

    @Test fun testFloatDouble() = doTest("Simple/FloatDouble.java")

    @Test fun testFor() = doTest("Simple/For.java")

    @Test fun testForEach() = doTest("Simple/ForEach.java")

    @Test fun testForEachMutableIterable() = doTest("Simple/ForEachMutableIterable.java")

    @Test fun testIdentityEquals() = doTest("Simple/IdentityEquals.java")

    @Test fun testImmutableField() = doTest("Simple/ImmutableField.java")

    @Test fun testIncDec() = doTest("Simple/IncDec.java")

    @Test fun testIntLong() = doTest("Simple/IntLong.java")

    @Test fun testLabeled() = doTest("Simple/Labeled.java")

    @Test fun testLabeledOuter() = doTest("Simple/LabeledOuter.java")

    @Test fun testLambda() = doTest("Simple/Lambda.java")

    @Test fun testLogicals() = doTest("Simple/Logicals.java")

    @Test fun testMethodReference() = doTest("Simple/MethodReference.java")

    @Test fun testModification() = doTest("Simple/Modification.java")

    @Test fun testMutableField() = doTest("Simple/MutableField.java")

    @Test fun testNotANumber() = doTest("Simple/NotANumber.java")

    @Test fun testReturnMinusX() = doTest("Simple/ReturnMinusX.java")

    @Test fun testReturnSum() = doTest("Simple/ReturnSum.java")

    @Test fun testReturnX() = doTest("Simple/ReturnX.java")

    @Test fun testShift() = doTest("Simple/Shift.java")

    @Test fun testStrings() = doTest("Simple/Strings.java")

    @Test fun testTernary() = doTest("Simple/Ternary.java")

    @Test fun testTryCatch() = doTest("Simple/TryCatch.java")

    @Test fun testWhile() = doTest("Simple/While.java")

    @Test fun testWhileWithContinue() = doTest("Simple/WhileWithContinue.java")

    @Test fun testWhileWithIncrement() = doTest("Simple/WhileWithIncrement.java")

    @Test fun testWhileWithMutableCondition() = doTest("Simple/WhileWithMutableCondition.java")

    @Test fun testWhileWithReturn() = doTest("Simple/WhileWithReturn.java")

    @Test fun testEvaluatorExtension() = doTest("Simple/EvaluatorExtension.java", object : UEvaluatorExtension {
        override val language: Language get() = JavaLanguage.INSTANCE

        override fun evaluateMethodCall(target: PsiMethod, argumentValues: List<UValue>, state: UEvaluationState): UEvaluationInfo {
            if (target.name == "getTestName") {
                (argumentValues.singleOrNull() as? UBooleanConstant)?.let { arg ->
                    return (if (arg.value) UStringConstant("UPPER") else UStringConstant("lower")) to state
                }
            }

            return super.evaluateMethodCall(target, argumentValues, state)
        }
    })

    @Test fun testParamViaEvaluatorExtension() = doTest("Simple/ParamViaEvaluatorExtension.java", object : UEvaluatorExtension {
        override val language: Language get() = JavaLanguage.INSTANCE

        override fun evaluateVariable(variable: UVariable, state: UEvaluationState): UEvaluationInfo {
            if (variable is UParameter && variable.name == "x") {
                return UStringConstant("0") to state
            }
            return super.evaluateVariable(variable, state)
        }
    })
}