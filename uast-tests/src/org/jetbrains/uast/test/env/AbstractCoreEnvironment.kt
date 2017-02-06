package org.jetbrains.uast.test.env

import com.intellij.mock.MockProject
import java.io.File

abstract class AbstractCoreEnvironment {
    abstract val project: MockProject

    open fun dispose() {
        // Do nothing
    }

    abstract fun addJavaSourceRoot(root: File)
}