package org.jetbrains.uast.test.java

import org.junit.Test

class JavaValuesTest : AbstractJavaValuesTest() {
    @Test fun testAliveThenElse() = doTest("Simple/AliveThenElse.java")

    @Test fun testAnonymous() = doTest("Simple/Anonymous.java")

    @Test fun testByteShort() = doTest("Simple/ByteShort.java")

    @Test fun testCascadeIf() = doTest("Simple/CascadeIf.java")

    @Test fun testCharacters() = doTest("Simple/Characters.java")

    @Test fun testDeadElse() = doTest("Simple/DeadElse.java")

    @Test fun testDeadFor() = doTest("Simple/DeadFor.java")

    @Test fun testDeadIfComparison() = doTest("Simple/DeadIfComparison.java")

    @Test fun testDeadSwitchEntries() = doTest("Simple/DeadSwitchEntries.java")

    @Test fun testDeadSwitchEntriesWithoutBreaks() = doTest("Simple/DeadSwitchEntriesWithoutBreaks.java")

    @Test fun testDeadThen() = doTest("Simple/DeadThen.java")

    @Test fun testDependents() = doTest("Simple/Dependents.java")

    @Test fun testDoWhile() = doTest("Simple/DoWhile.java")

    @Test fun testDoWhileInfinite() = doTest("Simple/DoWhileInfinite.java")

    @Test fun testEnumChoice() = doTest("Simple/EnumChoice.java")

    @Test fun testEnumSwitch() = doTest("Simple/EnumSwitch.java")

    @Test fun testEnumSwitchWithoutBreaks() = doTest("Simple/EnumSwitchWithoutBreaks.java")

    @Test fun testFloatDouble() = doTest("Simple/FloatDouble.java")

    @Test fun testFor() = doTest("Simple/For.java")

    @Test fun testForEach() = doTest("Simple/ForEach.java")

    @Test fun testImmutableField() = doTest("Simple/ImmutableField.java")

    @Test fun testIncDec() = doTest("Simple/IncDec.java")

    @Test fun testIntLong() = doTest("Simple/IntLong.java")

    @Test fun testLabeled() = doTest("Simple/Labeled.java")

    @Test fun testLambda() = doTest("Simple/Lambda.java")

    @Test fun testLogicals() = doTest("Simple/Logicals.java")

    @Test fun testMethodReference() = doTest("Simple/MethodReference.java")

    @Test fun testModification() = doTest("Simple/Modification.java")

    @Test fun testMutableField() = doTest("Simple/MutableField.java")

    @Test fun testReturnMinusX() = doTest("Simple/ReturnMinusX.java")

    @Test fun testReturnSum() = doTest("Simple/ReturnSum.java")

    @Test fun testReturnX() = doTest("Simple/ReturnX.java")

    @Test fun testStrings() = doTest("Simple/Strings.java")

    @Test fun testTernary() = doTest("Simple/Ternary.java")

    @Test fun testTryCatch() = doTest("Simple/TryCatch.java")

    @Test fun testWhile() = doTest("Simple/While.java")

    @Test fun testWhileWithIncrement() = doTest("Simple/WhileWithIncrement.java")
}