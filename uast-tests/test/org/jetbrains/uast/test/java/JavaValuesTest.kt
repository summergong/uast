package org.jetbrains.uast.test.java

import org.junit.Test

class JavaValuesTest : AbstractJavaValuesTest() {
    @Test fun testReturnX() = doTest("Simple/ReturnX.java")

    @Test fun testReturnSum() = doTest("Simple/ReturnSum.java")

    @Test fun testDeadElse() = doTest("Simple/DeadElse.java")

    @Test fun testAliveThenElse() = doTest("Simple/AliveThenElse.java")

    @Test fun testCascadeIf() = doTest("Simple/CascadeIf.java")
}