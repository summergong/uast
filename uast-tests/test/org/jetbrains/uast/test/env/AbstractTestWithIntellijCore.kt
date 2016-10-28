package org.jetbrains.uast.test.env

import com.intellij.core.JavaCoreProjectEnvironment
import com.intellij.mock.MockProject
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiManager
import com.intellij.rt.execution.junit.FileComparisonFailure
import junit.framework.TestCase
import org.jetbrains.uast.UastContext
import java.io.File

abstract class AbstractTestWithIntellijCore : TestCase() {
    private var myEnvironment : TestCoreEnvironment? = null
    private var myProjectEnvironment: JavaCoreProjectEnvironment? = null

    protected val environment: TestCoreEnvironment
        get() = myEnvironment!!

    protected val project: MockProject
        get() = myProjectEnvironment!!.project

    protected val uastContext: UastContext by lazy {
        ServiceManager.getService(project, UastContext::class.java)
    }

    protected val psiManager: PsiManager by lazy {
        PsiManager.getInstance(project)
    }

    override fun setUp() {
        initializeEnvironment()
        initializeProjectEnvironment()
    }

    override fun tearDown() {
        disposeEnvironment()
    }

    private fun initializeEnvironment() {
        if (myEnvironment != null) {
            error("Environment is already initialized")
        }
        myEnvironment = TestCoreEnvironment(Disposer.newDisposable())
    }

    private fun initializeProjectEnvironment() {
        if (myProjectEnvironment != null) {
            error("Project environment is already initialized")
        }
        myProjectEnvironment = myEnvironment!!.projectEnvironment
    }

    protected fun disposeEnvironment() {
        val environment = myEnvironment ?: error("Environment is already disposed")
        environment.dispose()
        myEnvironment = null
    }

    protected fun disposeProjectEnvironment() {
        val projectEnvironment = myProjectEnvironment ?: error("Project environment is already disposed")
        projectEnvironment.project.dispose()
        myProjectEnvironment = null
    }

    fun String.trimTrailingWhitespacesAndAddNewlineAtEOF(): String =
            this.split('\n').map(String::trimEnd).joinToString(separator = "\n").let {
                result -> if (result.endsWith("\n")) result else result + "\n"
            }

    protected fun assertEqualsToFile(description: String, expected: File, actual: String) {
        if (!expected.exists()) {
            expected.writeText(actual)
            fail("File didn't exist. New file was created (${expected.canonicalPath}).")
        }

        val expectedText =
                StringUtil.convertLineSeparators(expected.readText().trim()).trimTrailingWhitespacesAndAddNewlineAtEOF()
        val actualText =
                StringUtil.convertLineSeparators(actual.trim()).trimTrailingWhitespacesAndAddNewlineAtEOF()
        if (expectedText != actualText) {
            throw FileComparisonFailure(description, expectedText, actualText, expected.absolutePath)
        }
    }
}