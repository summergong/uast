package org.jetbrains.uast.test

import org.jetbrains.uast.test.java.AbstractJavaUastTest
import org.jetbrains.uast.UFile
import java.io.File

abstract class AbstractJavaTest : AbstractJavaUastTest() {
    private fun getTestFile(testName: String, ext: String) =
            File(File(TEST_JAVA_MODEL_DIR, testName).canonicalPath.substringBeforeLast('.') + '.' + ext)

    private fun getRenderFile(testName: String) = getTestFile(testName, "render.txt")
    private fun getLogFile(testName: String) = getTestFile(testName, "log.txt")

    override fun check(testName: String, file: UFile) {
        val renderFile = getRenderFile(testName)
        val logFile = getLogFile(testName)

        assertEqualsToFile(renderFile, file.asRenderString())
        assertEqualsToFile(logFile, file.asRenderString())
    }
}