package org.jetbrains.uast.test.kotlin

import org.junit.Test

class SimpleKotlinRenderLogTest : AbstractKotlinRenderLogTest() {
    @Test fun testSimple() = doTest("Simple")
}