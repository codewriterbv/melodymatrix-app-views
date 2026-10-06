package be.codewriter.melodymatrix.view.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Validates that every concrete [MmxView] subclass exposes complete
 * metadata: a declared bundle base name, a resolvable image, and a
 * non-blank `title` + `description` entry (with the optional key prefix)
 * in the English bundle. Presence of the other three locale files is
 * covered by [be.codewriter.melodymatrix.view.i18n.I18nBundleCoverageTest].
 */
class MmxViewMetadataTest {

    @TestFactory
    fun `all View implementations provide bundle metadata and a resolvable image`():
            List<DynamicTest> {
        val sourceFiles = findViewStageSourceFiles()
        assertTrue(sourceFiles.isNotEmpty(), "Expected at least one View implementation to validate")

        return sourceFiles.map { source ->
            DynamicTest.dynamicTest("${source.className} exposes bundleBaseName, image and English title/description") {
                val bundleBaseName = source.required(BUNDLE_BASE_NAME_REGEX, "bundleBaseName")
                val keyPrefix = source.optional(BUNDLE_KEY_PREFIX_REGEX).orEmpty()
                val imagePath = source.required(IMAGE_PATH_REGEX, "image path")

                assertTrue(bundleBaseName.isNotBlank(), "${source.className} must declare a non-blank bundleBaseName")
                assertTrue(imagePath.isNotBlank(), "${source.className} must declare a non-blank image path")

                verifyEnglishBundle(source.className, bundleBaseName, keyPrefix)
                verifyImageResource(source.className, imagePath)
            }
        }
    }

    private fun verifyEnglishBundle(className: String, bundleBaseName: String, keyPrefix: String) {
        val bundleFile = resourcesRoot.resolve("$bundleBaseName/en.properties")
        assertTrue(
            Files.exists(bundleFile),
            "$className bundle file '$bundleBaseName/en.properties' must exist under src/main/resources"
        )
        val props = Properties().apply { Files.newBufferedReader(bundleFile).use { load(it) } }
        val titleKey = "${keyPrefix}title"
        val descriptionKey = "${keyPrefix}description"
        val title = props.getProperty(titleKey)
        val description = props.getProperty(descriptionKey)
        assertNotNull(title, "$className must declare '$titleKey' in $bundleBaseName/en.properties")
        assertNotNull(description, "$className must declare '$descriptionKey' in $bundleBaseName/en.properties")
        assertTrue(title!!.isNotBlank(), "$className '$titleKey' must be non-blank")
        assertTrue(description!!.isNotBlank(), "$className '$descriptionKey' must be non-blank")
    }

    private fun verifyImageResource(className: String, imagePath: String) {
        val normalized = imagePath.removePrefix("/")
        val resourceFile = resourcesRoot.resolve(normalized)
        assertFalse(
            Files.isDirectory(resourceFile),
            "$className image path '$imagePath' should not be a directory"
        )
        assertTrue(
            Files.exists(resourceFile),
            "$className image path '$imagePath' must exist under src/main/resources"
        )
        assertNotNull(
            javaClass.classLoader.getResource(normalized),
            "$className image path '$imagePath' must be available on the test classpath"
        )
    }

    private fun findViewStageSourceFiles(): List<ViewStageSource> {
        val sourceFiles = mutableListOf<ViewStageSource>()

        Files.walk(sourceRoot).use { paths ->
            paths
                .filter { Files.isRegularFile(it) && it.toString().endsWith(".kt") }
                .forEach { path ->
                    val content = Files.readString(path)
                    val mmxViewImplementation = MMX_VIEW_IMPLEMENTATION_REGEX.find(content)
                    if (mmxViewImplementation != null && !ABSTRACT_VIEW_STAGE_REGEX.containsMatchIn(content)) {
                        sourceFiles += ViewStageSource(
                            path = path,
                            className = mmxViewImplementation.groupValues[1],
                            content = content
                        )
                    }
                }
        }

        return sourceFiles.sortedBy { it.className }
    }

    private fun ViewStageSource.required(pattern: Regex, label: String): String {
        return pattern.find(content)?.groupValues?.get(1)
            ?: error("${className} is missing $label metadata in ${path.fileName}")
    }

    private fun ViewStageSource.optional(pattern: Regex): String? =
        pattern.find(content)?.groupValues?.get(1)

    private data class ViewStageSource(
        val path: Path,
        val className: String,
        val content: String
    )

    companion object {
        private val classesRoot: Path = Paths.get(MmxView::class.java.protectionDomain.codeSource.location.toURI())
        private val moduleRoot: Path = classesRoot.parent.parent
        private val sourceRoot: Path = moduleRoot.resolve("src/main/kotlin")
        private val resourcesRoot: Path = moduleRoot.resolve("src/main/resources")

        private val ABSTRACT_VIEW_STAGE_REGEX = Regex("""abstract\s+class\s+View\b""")
        private val MMX_VIEW_IMPLEMENTATION_REGEX = Regex("""\bclass\s+(\w+)\b[^\n{]*:\s*[^\n{]*\bMmxView\s*\(""")
        private val BUNDLE_BASE_NAME_REGEX =
            Regex("""override\s+val\s+bundleBaseName\s*=\s*"([^"]+)"""")
        private val BUNDLE_KEY_PREFIX_REGEX =
            Regex("""override\s+val\s+bundleKeyPrefix\s*=\s*"([^"]*)"""")
        private val IMAGE_PATH_REGEX =
            Regex("""override\s+fun\s+getViewImagePath\(\)\s*:\s*String\s*=\s*"([^"]*)"""")
    }
}
