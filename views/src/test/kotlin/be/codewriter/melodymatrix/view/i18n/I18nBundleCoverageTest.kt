package be.codewriter.melodymatrix.view.i18n

import be.codewriter.melodymatrix.view.view.MmxView
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertTrue

/**
 * Ensures that every i18n bundle directory under `src/main/resources/i18n`
 * exposes the same set of keys across `en`, `nl`, `fr`, and `de` locale
 * files, so no translation is silently missing.
 *
 * English (`en.properties`) is treated as the authoritative source; each
 * key it defines must also exist in the three other locale files, and
 * every value must be non-blank.
 */
class I18nBundleCoverageTest {

    @TestFactory
    fun `every bundle has EN, NL, FR and DE files with matching keys`(): List<DynamicTest> {
        val bundleDirs = findBundleDirectories()
        assertTrue(bundleDirs.isNotEmpty(), "Expected at least one i18n bundle under $i18nRoot")

        return bundleDirs.flatMap { dir ->
            LOCALES.map { locale ->
                DynamicTest.dynamicTest("${dir.relativeTo(i18nRoot)} has $locale coverage") {
                    val localeFile = dir.resolve("$locale.properties")
                    assertTrue(
                        Files.exists(localeFile),
                        "Missing bundle file: $localeFile"
                    )
                    val localeProps = Properties().apply {
                        Files.newBufferedReader(localeFile).use { load(it) }
                    }
                    val englishProps = Properties().apply {
                        Files.newBufferedReader(dir.resolve("en.properties")).use { load(it) }
                    }
                    englishProps.stringPropertyNames().forEach { key ->
                        val value = localeProps.getProperty(key)
                        assertTrue(
                            value != null,
                            "Bundle ${dir.relativeTo(i18nRoot)}/$locale.properties is missing key '$key'"
                        )
                        assertTrue(
                            value!!.isNotBlank(),
                            "Bundle ${dir.relativeTo(i18nRoot)}/$locale.properties has blank value for key '$key'"
                        )
                    }
                }
            }
        }
    }

    private fun findBundleDirectories(): List<Path> {
        val dirs = mutableListOf<Path>()
        Files.walk(i18nRoot).use { paths ->
            paths.filter { Files.isDirectory(it) && Files.exists(it.resolve("en.properties")) }
                .forEach { dirs.add(it) }
        }
        return dirs.sorted()
    }

    private fun Path.relativeTo(base: Path): Path = base.relativize(this)

    companion object {
        private val classesRoot: Path = Paths.get(MmxView::class.java.protectionDomain.codeSource.location.toURI())
        private val moduleRoot: Path = classesRoot.parent.parent
        private val i18nRoot: Path = moduleRoot.resolve("src/main/resources/i18n")
        private val LOCALES = listOf("nl", "fr", "de", "es", "it", "pt", "ja", "zh", "pl")
    }
}
