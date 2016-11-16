package org.jetbrains.uast.test.kotlin

import org.junit.Test

class KotlinTypesTest : AbstractKotlinTypesTest() {
    @Test fun testLocalClass() = doTest("LocalClass")
}