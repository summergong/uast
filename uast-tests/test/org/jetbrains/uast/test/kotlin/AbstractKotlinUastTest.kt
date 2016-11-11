package org.jetbrains.uast.test.kotlin

import com.intellij.mock.MockProject
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.CliLightClassGenerationSupport
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.JavaSourceRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.getModuleName
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.addKotlinSourceRoot
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.resolve.jvm.extensions.AnalysisCompletedHandlerExtension
import org.jetbrains.kotlin.utils.PathUtil
import org.jetbrains.uast.kotlin.internal.UastAnalysisCompletedHandlerExtension
import org.jetbrains.uast.test.env.AbstractCoreEnvironment
import org.jetbrains.uast.test.env.AbstractUastTest
import java.io.File

abstract class AbstractKotlinUastTest : AbstractUastTest() {
    protected companion object {
        val TEST_KOTLIN_MODEL_DIR = File(TEST_DATA_DIR, "kotlin")
    }

    private var kotlinCoreEnvironment: KotlinCoreEnvironment? = null

    override fun getVirtualFile(testName: String): VirtualFile {
        val projectDir = TEST_KOTLIN_MODEL_DIR
        val testFile = File(TEST_KOTLIN_MODEL_DIR, testName.substringBefore('/') + ".kt")

        super.initializeEnvironment(testFile)

        val trace = CliLightClassGenerationSupport.NoScopeRecordCliBindingTrace()

        val kotlinCoreEnvironment = kotlinCoreEnvironment!!

        val moduleContext = TopDownAnalyzerFacadeForJVM.createContextWithSealedModule(
                environment.project, kotlinCoreEnvironment.getModuleName())

        TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegrationNoIncremental(
                moduleContext,
                kotlinCoreEnvironment.getSourceFiles(),
                trace,
                TopDownAnalysisMode.TopLevelDeclarations,
                JvmPackagePartProvider(kotlinCoreEnvironment)
        )

        val vfs = StandardFileSystems.local()

        val ideaProject = project
        ideaProject.baseDir = vfs.findFileByPath(projectDir.canonicalPath)

        return vfs.findFileByPath(testFile.canonicalPath)!!
    }

    override fun createEnvironment(source: File): AbstractCoreEnvironment {
        val kotlinCoreEnvironment = KotlinCoreEnvironment.createForTests(
                Disposer.newDisposable(),
                createKotlinCompilerConfiguration(source),
                EnvironmentConfigFiles.JVM_CONFIG_FILES)

        this.kotlinCoreEnvironment = kotlinCoreEnvironment

        AnalysisCompletedHandlerExtension.registerExtension(
                kotlinCoreEnvironment.project, UastAnalysisCompletedHandlerExtension())

        return KotlinCoreEnvironmentWrapper(kotlinCoreEnvironment)
    }

    override fun tearDown() {
        kotlinCoreEnvironment = null
        super.tearDown()
    }

    private fun createKotlinCompilerConfiguration(sourceFile: File): CompilerConfiguration {
        val configuration = CompilerConfiguration()
        configuration.addJvmClasspathRoots(PathUtil.getJdkClassesRoots())

        val kotlinLibsDir = File("../lib/kotlin-plugin/Kotlin/kotlinc/lib")
        configuration.addJvmClasspathRoot(File(kotlinLibsDir, "kotlin-runtime.jar"))
        configuration.addJvmClasspathRoot(File(kotlinLibsDir, "kotlin-reflect.jar"))

        configuration.addKotlinSourceRoot(sourceFile.canonicalPath)

        val messageCollector = PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, true)
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)

        configuration.put<String>(JVMConfigurationKeys.MODULE_NAME, "test-module")

        return configuration
    }

    private class KotlinCoreEnvironmentWrapper(val environment: KotlinCoreEnvironment) : AbstractCoreEnvironment() {
        override val project: MockProject
            get() = environment.project as MockProject

        override fun addJavaSourceRoot(root: File) {
            environment.addJavaSourceRoots(listOf(JavaSourceRoot(root, null)))
        }
    }
}