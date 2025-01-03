package com.github.l34130.mise.core.command

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.diagnostic.Logger

internal class MiseCommandLine(
    private val workDir: String? = null,
    private val configEnvironment: String? = null,
) {
    inline fun <reified T> runCommandLine(vararg params: String): Result<T> =
        runCommandLine(params.toList())

    inline fun <reified T> runCommandLine(params: List<String>): Result<T> {
        val miseVersion = getMiseVersion()

        val commandLineArgs = mutableListOf("mise")

        if (configEnvironment != null) {
            if (miseVersion >= MiseVersion(2024, 12, 2)) {
                commandLineArgs.add("--env")
                commandLineArgs.add(configEnvironment)
            } else {
                commandLineArgs.add("--profile")
                commandLineArgs.add(configEnvironment)
            }
        }

        commandLineArgs.addAll(params)

        return if (T::class == String::class) {
            runCommandLine(commandLineArgs) { it as T }
        } else {
            runCommandLine(commandLineArgs) {
                GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create()
                    .fromJson(it, object : TypeToken<T>() {})
            }
        }
    }

    private fun <T> runCommandLine(commandLineArgs: List<String>, transform: (String) -> T): Result<T> {
        val generalCommandLine = GeneralCommandLine(commandLineArgs).withWorkDirectory(workDir)
        val processOutput = try {
            logger.debug("Running command: $commandLineArgs")
            ExecUtil.execAndGetOutput(generalCommandLine, 5000)
        } catch (e: ExecutionException) {
            logger.info("Failed to execute command. (command=$generalCommandLine)", e)
            return Result.failure(
                MiseCommandLineNotFoundException(
                    generalCommandLine,
                    e.message ?: "Failed to execute command.",
                    e
                )
            )
        }

        if (!processOutput.isExitCodeSet) {
            when {
                processOutput.isTimeout -> {
                    return Result.failure(Throwable("Command timed out. (command=$generalCommandLine)"))
                }

                processOutput.isCancelled -> {
                    return Result.failure(Throwable("Command was cancelled. (command=$generalCommandLine)"))
                }
            }
        }

        if (processOutput.exitCode != 0) {
            val stderr = processOutput.stderr
            val parsedError = MiseCommandLineException.parseFromStderr(generalCommandLine, stderr)
            if (parsedError == null) {
                logger.info("Failed to parse error from stderr. (stderr=$stderr)")
                return Result.failure(Throwable(stderr))
            } else {
                logger.debug("Parsed error from stderr. (error=$parsedError)")
                return Result.failure(parsedError)
            }
        }

        logger.debug("Command executed successfully. (command=$generalCommandLine)")
        return Result.success(transform(processOutput.stdout))
    }

    companion object {
        fun getMiseVersion(): MiseVersion {
            val miseCommandLine = MiseCommandLine()
            val versionString = miseCommandLine.runCommandLine(listOf("mise", "version")) { it }

            val miseVersion = versionString.fold(
                onSuccess = {
                    MiseVersion.parse(it)
                },
                onFailure = { _ ->
                    MiseVersion(0, 0, 0)
                }
            )

            return miseVersion
        }

        private val logger = Logger.getInstance(MiseCommandLine::class.java)
    }
}
