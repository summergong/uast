package org.jetbrains.uast.test.java

import org.jetbrains.uast.test.common.ValuesTestBase
import java.io.File

abstract class AbstractJavaValuesTest : AbstractJavaUastTest(), ValuesTestBase {

    private fun getTestFile(testName: String, ext: String) =
            File(File(TEST_JAVA_MODEL_DIR, testName).canonicalPath.substringBeforeLast('.') + '.' + ext)

    override fun getValuesFile(testName: String) = getTestFile(testName, "values.txt")
}