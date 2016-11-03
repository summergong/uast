package org.jetbrains.uast.test.java

import org.junit.Test

class JavaValuesTest : AbstractJavaValuesTest() {
    @Test fun testAliveThenElse() = doTest("Simple/AliveThenElse.java")

    @Test fun testCascadeIf() = doTest("Simple/CascadeIf.java")

    @Test fun testDeadElse() = doTest("Simple/DeadElse.java")

    @Test fun testDeadFor() = doTest("Simple/DeadFor.java")

    @Test fun testDeadIfComparison() = doTest("Simple/DeadIfComparison.java")

    @Test fun testDeadSwitchEntries() = doTest("Simple/DeadSwitchEntries.java")

    @Test fun testDeadThen() = doTest("Simple/DeadThen.java")

    @Test fun testDependents() = doTest("Simple/Dependents.java")

    @Test fun testEnumChoice() = doTest("Simple/EnumChoice.java")

    @Test fun testEnumSwitch() = doTest("Simple/EnumSwitch.java")

    @Test fun testFor() = doTest("Simple/For.java")

    @Test fun testForEach() = doTest("Simple/ForEach.java")

    @Test fun testReturnMinusX() = doTest("Simple/ReturnMinusX.java")

    @Test fun testReturnSum() = doTest("Simple/ReturnSum.java")

    @Test fun testReturnX() = doTest("Simple/ReturnX.java")

    @Test fun testStrings() = doTest("Simple/Strings.java")

}