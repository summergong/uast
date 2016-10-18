package org.jetbrains.uast.test.java

import org.junit.Test

class JavaValuesTest : AbstractJavaValuesTest() {

    @Test
    fun testReturnX() {
        doTest("Simple/ReturnX.java")
    }
}