package org.jetbrains.uast.test.java

import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.uast.test.env.AbstractCoreEnvironment
import org.jetbrains.uast.test.env.AbstractUastTest
import org.jetbrains.uast.test.env.TestCoreEnvironment
import java.io.File

abstract class AbstractJavaUastTest : AbstractUastTest() {
    protected companion object {
        val TEST_JAVA_MODEL_DIR = File(TEST_DATA_DIR, "java")
    }

    override fun getVirtualFile(testName: String): VirtualFile {
        val projectDir = TEST_JAVA_MODEL_DIR
        val testSourcesDir = File(TEST_JAVA_MODEL_DIR, testName.substringBefore('/'))

        super.initializeEnvironment(testSourcesDir)

        val vfs = StandardFileSystems.local()
        environment.addJavaSourceRoot(testSourcesDir)
        val ideaProject = project
        ideaProject.baseDir = vfs.findFileByPath(projectDir.canonicalPath)

        return vfs.findFileByPath(File(TEST_JAVA_MODEL_DIR, testName).canonicalPath)!!
    }

    override fun createEnvironment(source: File): AbstractCoreEnvironment {
        return TestCoreEnvironment(Disposer.newDisposable())
    }
}