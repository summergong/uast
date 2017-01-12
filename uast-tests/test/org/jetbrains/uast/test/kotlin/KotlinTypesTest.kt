package org.jetbrains.uast.test.kotlin

import org.junit.Test

class KotlinTypesTest : AbstractKotlinTypesTest() {
    @Test fun testLocalDeclarations() = doTest("LocalDeclarations")

    @Test fun testUnexpectedContainerException() = doTest("UnexpectedContainerException")
}