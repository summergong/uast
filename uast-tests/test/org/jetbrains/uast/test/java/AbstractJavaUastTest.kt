package org.jetbrains.uast.test.java

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import org.jetbrains.uast.test.env.AbstractUastTest
import java.io.File

abstract class AbstractJavaUastTest : AbstractUastTest() {
    protected companion object {
        val TEST_JAVA_MODEL_DIR = File(TEST_DATA_DIR, "java")
    }

    override fun getVirtualFile(testName: String): VirtualFile {
        val projectDir = TEST_JAVA_MODEL_DIR
        val testSourcesDir = File(TEST_JAVA_MODEL_DIR, testName.substringBefore('/'))

        val vfs = StandardFileSystems.local()
        addDirectoryToClassPath(vfs, testSourcesDir)
        val ideaProject = environment.project
        ideaProject.baseDir = vfs.findFileByPath(projectDir.canonicalPath)

        return vfs.findFileByPath(File(TEST_JAVA_MODEL_DIR, testName).canonicalPath)!!
    }

    private fun addDirectoryToClassPath(vfs: VirtualFileSystem, testSourcesDir: File) {
        val virtualFile = vfs.findFileByPath(testSourcesDir.canonicalPath)!!
        environment.projectEnvironment.addSourcesToClasspath(virtualFile)
    }
}