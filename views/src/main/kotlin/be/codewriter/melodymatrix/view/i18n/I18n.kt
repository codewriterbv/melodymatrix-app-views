package be.codewriter.melodymatrix.view.i18n

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.MessageFormat
import java.util.Locale
import java.util.PropertyResourceBundle
import java.util.ResourceBundle
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Central runtime for translation lookups and live-updating JavaFX bindings.
 *
 * ### Bundle layout
 * Each logical surface owns a directory under `i18n/` containing one
 * `.properties` file per supported language:
 * ```
 * i18n/view/piano/en.properties
 * i18n/view/piano/nl.properties
 * i18n/view/piano/fr.properties
 * i18n/view/piano/de.properties
 * ```
 * The `baseName` passed to [registerBundle] is the directory path
 * (e.g. `"i18n/view/piano"`); the language tag is appended internally.
 *
 * ### Lifecycle
 * The active language is stored in [currentLocale]. UI code should:
 * 1. Register a bundle once (typically in a companion or class init):
 *    ```kotlin
 *    private val bundle = I18n.registerBundle("i18n/view/piano")
 *    ```
 * 2. Bind text properties to [binding] so labels update live when the
 *    language changes:
 *    ```kotlin
 *    label.textProperty().bind(I18n.binding(bundle, "toolbar.image"))
 *    ```
 * 3. Use [get] for one-shot text (log labels, exception messages, dialog
 *    titles that are not stored on a JavaFX property).
 *
 * ### Fallbacks
 * Lookups fall back to the base (English) bundle when the active locale
 * does not contain a key; when both fail, `!key!` is returned and a
 * warning is logged once per (bundle, key) tuple.
 *
 * ### Encoding
 * `.properties` files are read as UTF-8, so accented characters can be
 * written directly (no `\uXXXX` escaping needed).
 */
object I18n {

    private val logger: Logger = LogManager.getLogger(I18n::class.java.name)

    /**
     * Currently active language. Mutating this property triggers a refresh
     * of every registered bundle and, in turn, every [StringBinding]
     * derived from it.
     */
    val currentLocale: SimpleObjectProperty<SupportedLocale> =
        SimpleObjectProperty(detectSystemLocale())

    private val bundleCache: MutableMap<BundleCacheKey, ResourceBundle> = ConcurrentHashMap()
    private val registeredBundles: MutableList<BundleRef> = CopyOnWriteArrayList()
    private val missingKeyWarnings: MutableSet<String> = ConcurrentHashMap.newKeySet()

    init {
        currentLocale.addListener { _, _, _ -> refreshAll() }
    }

    /**
     * Detects the system-configured language and returns the matching
     * [SupportedLocale]; falls back to [SupportedLocale.EN] when the OS
     * language is not supported by the app.
     */
    fun detectSystemLocale(): SupportedLocale {
        val defaultLang = Locale.getDefault().language
        return SupportedLocale.fromTag(defaultLang) ?: SupportedLocale.EN
    }

    /**
     * Registers a bundle for a given [baseName] (e.g. `"i18n/view/piano"`).
     *
     * The returned [BundleRef] can be reused across multiple lookups /
     * bindings; identical `baseName` + `classLoader` pairs share a single
     * ref. On locale change every registered ref is refreshed atomically.
     */
    @JvmOverloads
    fun registerBundle(
        baseName: String,
        classLoader: ClassLoader = I18n::class.java.classLoader
    ): BundleRef {
        val normalized = baseName.trimEnd('/')
        registeredBundles.firstOrNull { it.baseName == normalized && it.classLoader === classLoader }
            ?.let { return it }
        val ref = BundleRef(normalized, classLoader)
        registeredBundles += ref
        return ref
    }

    /**
     * Looks up [key] in [ref]'s active bundle, formatting the result with
     * `MessageFormat` when [args] are supplied. Falls back to the base
     * (English) bundle when the key is missing in the active locale, and
     * to `!key!` when it is missing everywhere.
     */
    fun get(ref: BundleRef, key: String, vararg args: Any?): String {
        val raw = ref.getOrNull(key)
            ?: fallbackLookup(ref, key)
            ?: run {
                warnMissing(ref, key)
                return "!$key!"
            }
        return if (args.isEmpty()) raw
        else MessageFormat(raw, currentLocale.get().locale).format(args)
    }

    /**
     * Returns a [StringBinding] that re-evaluates whenever [ref]'s
     * underlying bundle is refreshed. Suitable for `textProperty()` /
     * `titleProperty()` / tooltip text.
     */
    fun binding(ref: BundleRef, key: String, vararg args: Any?): StringBinding {
        // Copy varargs so callers can safely mutate their array.
        val safeArgs = args.copyOf()
        return Bindings.createStringBinding(
            { get(ref, key, *safeArgs) },
            ref.bundle
        )
    }

    // --- internal ---

    internal fun loadBundle(baseName: String, locale: SupportedLocale, classLoader: ClassLoader): ResourceBundle {
        val key = BundleCacheKey(baseName, locale, System.identityHashCode(classLoader))
        return bundleCache.getOrPut(key) { readProperties(baseName, locale, classLoader) }
    }

    private fun readProperties(
        baseName: String,
        locale: SupportedLocale,
        classLoader: ClassLoader
    ): ResourceBundle {
        val path = "$baseName/${locale.tag}.properties"
        val stream = classLoader.getResourceAsStream(path)
        if (stream == null) {
            // Only warn the first time we notice a missing file per (bundle, locale).
            val warnKey = "file::$path"
            if (missingKeyWarnings.add(warnKey)) {
                logger.warn("Missing i18n bundle file '{}' (locale={})", path, locale)
            }
            return EmptyResourceBundle
        }
        return stream.use { input ->
            PropertyResourceBundle(InputStreamReader(input, StandardCharsets.UTF_8))
        }
    }

    private fun refreshAll() {
        val locale = currentLocale.get()
        registeredBundles.forEach { ref ->
            val fresh = loadBundle(ref.baseName, locale, ref.classLoader)
            ref.bundleProperty.set(fresh)
        }
    }

    private fun fallbackLookup(ref: BundleRef, key: String): String? {
        if (currentLocale.get() == SupportedLocale.EN) return null
        val english = loadBundle(ref.baseName, SupportedLocale.EN, ref.classLoader)
        return if (english.containsKey(key)) english.getString(key) else null
    }

    private fun warnMissing(ref: BundleRef, key: String) {
        val warnKey = "${ref.baseName}::$key"
        if (missingKeyWarnings.add(warnKey)) {
            logger.warn("Missing i18n key '{}' in bundle '{}' (locale={})", key, ref.baseName, currentLocale.get())
        }
    }

    private data class BundleCacheKey(
        val baseName: String,
        val locale: SupportedLocale,
        val classLoaderId: Int
    )

    private object EmptyResourceBundle : ResourceBundle() {
        override fun handleGetObject(key: String): Any? = null
        override fun getKeys() = java.util.Collections.emptyEnumeration<String>()
    }
}
