package org.jetbrains.uast.test.java

import org.jetbrains.uast.UFile
import org.jetbrains.uast.ULocalVariable
import org.jetbrains.uast.test.env.findElementByText
import org.junit.Test

class JavaUastApiTest : AbstractJavaUastTest() {
    override fun check(testName: String, file: UFile) {
    }

    @Test fun testTypeReference() {
        doTest("Simple/TypeReference.java") { name, file ->
            val localVar = file.findElementByText<ULocalVariable>("String s;")
            val typeRef = localVar.typeReference
            assertNotNull(typeRef)
        }
    }

    @Test fun testFields() {
        doTest("Simple/Field.java") { name, file ->
            assertEquals(1, file.classes[0].fields.size)
        }
    }
}
