package org.jetbrains.uast.test.kotlin

import org.junit.Test

class KotlinValuesTest : AbstractKotlinValuesTest() {

    @Test fun testAssertion() = doTest("Assertion")

    @Test fun testIn() = doTest("In")

    @Test fun testLocalClass() = doTest("LocalClass")

    @Test fun testSimple() = doTest("Simple")
}