package org.jetbrains.uast.test.java

import org.jetbrains.uast.test.common.ExpressionsTestBase
import java.io.File

abstract class AbstractJavaExpressionsTest : AbstractJavaUastTest(), ExpressionsTestBase {

    private fun getTestFile(testName: String, ext: String) =
            File(File(TEST_JAVA_MODEL_DIR, testName).canonicalPath.substringBeforeLast('.') + '.' + ext)

    override fun getValuesFile(testName: String) = getTestFile(testName, "expressions.txt")
}