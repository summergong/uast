package org.jetbrains.uast.test.java

import org.junit.Test

class SimpleJavaRenderLogTest : AbstractJavaRenderLogTest() {
    @Test fun testDataClass() = doTest("DataClass/DataClass.java")

    @Test fun testEnumSwitch() = doTest("Simple/EnumSwitch.java")

    @Test fun testLocalClass() = doTest("Simple/LocalClass.java")

    @Test fun testReturnX() = doTest("Simple/ReturnX.java")

    @Test fun testJava() = doTest("Simple/Simple.java")

    @Test fun testClass() = doTest("Simple/SuperTypes.java")

    @Test fun testTryWithResources() = doTest("Simple/TryWithResources.java")

    @Test fun testEnumValueMembers() = doTest("Simple/EnumValueMembers.java")
}
