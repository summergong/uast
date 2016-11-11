package org.jetbrains.uast.test.java

import org.jetbrains.uast.test.java.AbstractJavaUastTest
import org.jetbrains.uast.UFile
import org.jetbrains.uast.asRecursiveLogString
import java.io.File

abstract class AbstractJavaRenderLogTest : AbstractJavaUastTest() {
    private fun getTestFile(testName: String, ext: String) =
            File(File(TEST_JAVA_MODEL_DIR, testName).canonicalPath.substringBeforeLast('.') + '.' + ext)

    private fun getRenderFile(testName: String) = getTestFile(testName, "render.txt")
    private fun getLogFile(testName: String) = getTestFile(testName, "log.txt")

    override fun check(testName: String, file: UFile) {
        val renderFile = getRenderFile(testName)
        val logFile = getLogFile(testName)

        assertEqualsToFile("Render string", renderFile, file.asRenderString())
        assertEqualsToFile("Log string", logFile, file.asRecursiveLogString())
    }
}