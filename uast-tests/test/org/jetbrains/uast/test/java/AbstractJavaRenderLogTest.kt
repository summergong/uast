package org.jetbrains.uast.test.java

import org.jetbrains.uast.test.common.RenderLogTestBase
import java.io.File

abstract class AbstractJavaRenderLogTest : AbstractJavaUastTest(), RenderLogTestBase {
    override fun getTestFile(testName: String, ext: String) =
            File(File(TEST_JAVA_MODEL_DIR, testName).canonicalPath.substringBeforeLast('.') + '.' + ext)
}