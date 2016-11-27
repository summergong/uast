package org.jetbrains.uast.test.kotlin

import org.junit.Test

class SimpleKotlinRenderLogTest : AbstractKotlinRenderLogTest() {
    @Test fun testLocalClass() = doTest("LocalClass")

    @Test fun testSimple() = doTest("Simple")

    @Test fun testWhenIs() = doTest("WhenIs")
}