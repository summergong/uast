package org.jetbrains.uast.test.java

import org.jetbrains.uast.test.AbstractJavaTest
import org.junit.Test

class SimpleJavaTest : AbstractJavaTest() {
    @Test fun testDataClass() = doTest("DataClass/DataClass.java")

    @Test fun testEnumSwitch() = doTest("Simple/EnumSwitch.java")

    @Test fun testJava() = doTest("Simple/Simple.java")

    @Test fun testReturnX() = doTest("Simple/ReturnX.java")
}