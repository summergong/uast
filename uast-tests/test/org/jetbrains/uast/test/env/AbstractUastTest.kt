package org.jetbrains.uast.test.env

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.uast.UFile
import java.io.File

abstract class AbstractUastTest : AbstractTestWithCoreEnvironment() {
    protected companion object {
        val TEST_DATA_DIR = File("testData")
    }

    abstract fun getVirtualFile(testName: String): VirtualFile
    abstract fun check(testName: String, file: UFile)

    fun doTest(testName: String) {
        val virtualFile = getVirtualFile(testName)

        val vfs = StandardFileSystems.local()
        project.baseDir = vfs.findFileByPath(TEST_DATA_DIR.canonicalPath)

        val psiFile = psiManager.findFile(virtualFile) ?: error("Can't get psi file for $testName")
        val uFile = uastContext.convertElementWithParent(psiFile, null) ?: error("Can't get UFile for $testName")
        check(testName, uFile as UFile)
    }
}