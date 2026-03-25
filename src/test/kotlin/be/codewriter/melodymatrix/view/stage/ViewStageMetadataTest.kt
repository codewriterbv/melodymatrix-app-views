package be.codewriter.melodymatrix.view.stage

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ViewStageMetadataTest {

    @TestFactory
    fun `all ViewStage implementations provide complete metadata and a resolvable image`():
            List<DynamicTest> {
        val sourceFiles = findViewStageSourceFiles()
        assertTrue(sourceFiles.isNotEmpty(), "Expected at least one ViewStage implementation to validate")

        return sourceFiles.map { source ->
            DynamicTest.dynamicTest("${source.className} exposes title, description, and image resource") {
                val title = source.requiredString(TITLE_REGEX, "title")
                val description = source.requiredString(DESCRIPTION_REGEX, "description")
                val imagePath = source.requiredString(IMAGE_PATH_REGEX, "image path")
                val normalizedImagePath = imagePath.removePrefix("/")

                assertTrue(title.isNotBlank(), "${source.className} must declare a non-blank title")
                assertTrue(description.isNotBlank(), "${source.className} must declare a non-blank description")
                assertTrue(imagePath.isNotBlank(), "${source.className} must declare a non-blank image path")

                val resourceFile = resourcesRoot.resolve(normalizedImagePath)
                assertFalse(
                    Files.isDirectory(resourceFile),
                    "${source.className} image path '$imagePath' should not be a directory"
                )
                assertTrue(
                    Files.exists(resourceFile),
                    "${source.className} image path '$imagePath' must exist under src/main/resources"
                )
                assertNotNull(
                    javaClass.classLoader.getResource(normalizedImagePath),
                    "${source.className} image path '$imagePath' must be available on the test classpath"
                )
            }
        }
    }

    private fun findViewStageSourceFiles(): List<ViewStageSource> {
        val sourceFiles = mutableListOf<ViewStageSource>()

        Files.walk(sourceRoot).use { paths ->
            paths
                .filter { Files.isRegularFile(it) && it.toString().endsWith(".kt") }
                .forEach { path ->
                    val content = Files.readString(path)
                    if (
                        VIEW_STAGE_IMPLEMENTATION_REGEX.containsMatchIn(content) &&
                        !ABSTRACT_VIEW_STAGE_REGEX.containsMatchIn(content)
                    ) {
                        sourceFiles += ViewStageSource(
                            path = path,
                            className = extractClassName(content, path),
                            content = content
                        )
                    }
                }
        }

        return sourceFiles.sortedBy { it.className }
    }

    private fun extractClassName(content: String, path: Path): String {
        return CLASS_NAME_REGEX.find(content)?.groupValues?.get(1)
            ?: error("Unable to determine ViewStage implementation name from ${path.fileName}")
    }

    private fun ViewStageSource.requiredString(pattern: Regex, label: String): String {
        return pattern.find(content)?.groupValues?.get(1)
            ?: error("${className} is missing $label metadata in ${path.fileName}")
    }

    private data class ViewStageSource(
        val path: Path,
        val className: String,
        val content: String
    )

    companion object {
        private val classesRoot: Path = Paths.get(ViewStage::class.java.protectionDomain.codeSource.location.toURI())
        private val moduleRoot: Path = classesRoot.parent.parent
        private val sourceRoot: Path = moduleRoot.resolve("src/main/kotlin")
        private val resourcesRoot: Path = moduleRoot.resolve("src/main/resources")

        private val ABSTRACT_VIEW_STAGE_REGEX = Regex("""abstract\s+class\s+ViewStage\b""")
        private val VIEW_STAGE_IMPLEMENTATION_REGEX = Regex(""":\s*[\s\S]*?ViewStage\s*\(""")
        private val CLASS_NAME_REGEX = Regex("""\bclass\s+(\w+)\b""")
        private val TITLE_REGEX = Regex("""override\s+fun\s+getViewTitle\(\)\s*:\s*String\s*=\s*\"([^\"]+)\"""")
        private val DESCRIPTION_REGEX =
            Regex("""override\s+fun\s+getViewDescription\(\)\s*:\s*String\s*=\s*\"([^\"]+)\"""")
        private val IMAGE_PATH_REGEX =
            Regex("""override\s+fun\s+getViewImagePath\(\)\s*:\s*String\s*=\s*\"([^\"]*)\"""")
    }
}

