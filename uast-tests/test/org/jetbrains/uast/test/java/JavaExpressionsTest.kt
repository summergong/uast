package org.jetbrains.uast.test.java

import org.junit.Test

class JavaExpressionsTest : AbstractJavaExpressionsTest() {

    @Test
    fun testAliveThenElse() = doTest("Simple/AliveThenElse.java")
}
