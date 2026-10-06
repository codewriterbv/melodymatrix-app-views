package be.codewriter.melodymatrix.view.i18n

import javafx.beans.property.ReadOnlyObjectProperty
import javafx.beans.property.SimpleObjectProperty
import java.util.ResourceBundle

/**
 * Handle to a `ResourceBundle` that is transparently refreshed by [I18n]
 * whenever the active locale changes.
 *
 * Holds the [baseName] used to look up `.properties` files and the
 * [classLoader] that owns them (so bundles work when Viewers, MainApplication
 * and their tests are loaded from independent class-loaders). The
 * internal [bundleProperty] carries the currently resolved [ResourceBundle]
 * and is what [I18n.binding] observes to make JavaFX `StringBinding`s
 * re-evaluate on language change.
 */
class BundleRef internal constructor(
    val baseName: String,
    internal val classLoader: ClassLoader
) {
    internal val bundleProperty: SimpleObjectProperty<ResourceBundle> =
        SimpleObjectProperty(I18n.loadBundle(baseName, I18n.currentLocale.get(), classLoader))

    /** Observable view of the currently resolved bundle (never `null`). */
    val bundle: ReadOnlyObjectProperty<ResourceBundle> get() = bundleProperty

    /** Currently resolved [ResourceBundle] for this handle. */
    fun current(): ResourceBundle = bundleProperty.get()

    /**
     * Returns the value for [key] in the active bundle, or `null` when the
     * key is not present. Never falls back to English — use [I18n.get]
     * for that behaviour.
     */
    fun getOrNull(key: String): String? {
        val bundle = current()
        return if (bundle.containsKey(key)) bundle.getString(key) else null
    }
}
