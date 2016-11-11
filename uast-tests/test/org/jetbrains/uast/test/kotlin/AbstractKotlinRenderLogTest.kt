package org.jetbrains.uast.test.kotlin

import org.jetbrains.uast.UFile
import org.jetbrains.uast.asRecursiveLogString
import java.io.File

abstract class AbstractKotlinRenderLogTest : AbstractKotlinUastTest() {
    private fun getTestFile(testName: String, ext: String) =
            File(File(TEST_KOTLIN_MODEL_DIR, testName).canonicalPath.substringBeforeLast('.') + '.' + ext)

    private fun getRenderFile(testName: String) = getTestFile(testName, "render.txt")
    private fun getLogFile(testName: String) = getTestFile(testName, "log.txt")

    override fun check(testName: String, file: UFile) {
        val renderFile = getRenderFile(testName)
        val logFile = getLogFile(testName)

        assertEqualsToFile("Render string", renderFile, file.asRenderString())
        assertEqualsToFile("Log string", logFile, file.asRecursiveLogString())
    }
}