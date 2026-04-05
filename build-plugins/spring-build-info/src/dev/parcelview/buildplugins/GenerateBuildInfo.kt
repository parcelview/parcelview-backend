package dev.parcelview.buildplugins

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction

@TaskAction
fun generateBuildInfo(
    appName: String,
    appVersion: String,
    @Input projectRootDir: Path,
    @Output outputDir: Path,
) {
    val commit = gitCommit(projectRootDir)
    val file = outputDir.resolve("META-INF/build-info.properties")
    file.parent.createDirectories()
    file.writeText(
        """
        build.name=$appName
        build.version=$appVersion
        build.git.commit=${commit}
        build.git.branch=${gitBranch(projectRootDir)}
        build.git.time=${gitCommitTime(projectRootDir, commit)}
        """.trimIndent()
    )
}

private fun gitCommit(projectRootDir: Path): String =
    runGit(projectRootDir, "rev-parse", "--short", "HEAD")

private fun gitCommitTime(projectRootDir: Path, commit: String): String =
    runGit(projectRootDir, "log", "-1", "--format=%cI", commit)

private fun gitBranch(projectRootDir: Path): String =
    runGit(projectRootDir, "rev-parse", "--abbrev-ref", "HEAD")

private fun runGit(workingDir: Path, vararg args: String): String =
    try {
        ProcessBuilder("git", *args)
            .directory(workingDir.toFile())
            .redirectErrorStream(true)
            .start()
            .inputStream.bufferedReader().readText().trim()
    } catch (_: Exception) {
        "unknown"
    }