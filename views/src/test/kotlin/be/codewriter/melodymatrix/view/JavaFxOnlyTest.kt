package be.codewriter.melodymatrix.view

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

/**
 * The views are JavaFX only: no AWT, no Swing and no JavaFX/Swing bridge (`javafx.embed.swing`, which would also need
 * the `javafx-swing` module that this module does not depend on). Scans the sources of this module for such imports.
 */
class JavaFxOnlyTest {

    // Split up so that this file does not match itself
    private val forbidden = listOf("java" + ".awt", "javax" + ".swing", "javafx.embed" + ".swing", "javax" + ".imageio")

    @Test
    fun `sources use no awt or swing`() {
        val offenders = listOf("src/main", "src/test")
            .map(::File)
            .filter { it.exists() }
            .flatMap { root -> root.walkTopDown().filter { it.isFile && it.extension in setOf("kt", "java") }.toList() }
            .filterNot { it.name == "JavaFxOnlyTest.kt" }
            .flatMap { file ->
                file.readLines().withIndex()
                    .filter { (_, line) -> line.trimStart().startsWith("import ") && forbidden.any { line.contains(it) } }
                    .map { (index, line) -> "${file.path}:${index + 1}: ${line.trim()}" }
            }
        assertTrue(offenders.isEmpty(), "AWT/Swing is not allowed in the views (JavaFX only):\n" + offenders.joinToString("\n"))
    }
}
