package be.codewriter.melodymatrix.view.i18n

import java.util.Locale

/**
 * Enumerates the languages supported by MelodyMatrix.
 *
 * Each entry carries the resolved [Locale] used to look up `.properties`
 * bundles and a **native** [displayName] (rendered in the language's own
 * script) suitable for showing in the language selector combo box.
 *
 * The [flag] emoji is a purely decorative hint used by UI components; it
 * is safe to ignore if not needed.
 */
enum class SupportedLocale(
    val locale: Locale,
    val displayName: String,
    val flag: String
) {
    EN(Locale.ENGLISH, "English", "🇬🇧"),
    NL(Locale.of("nl"), "Nederlands", "🇳🇱"),
    FR(Locale.FRENCH, "Français", "🇫🇷"),
    DE(Locale.GERMAN, "Deutsch", "🇩🇪");

    /** Short language tag ("en", "nl", "fr", "de"). */
    val tag: String get() = locale.language

    /** `"🇳🇱 Nederlands"`-style label used by the language combo cell renderer. */
    val labelWithFlag: String get() = "$flag $displayName"

    companion object {
        /**
         * Resolves the [SupportedLocale] matching the given language [tag]
         * (e.g. `"en"`, `"nl"`, `"fr"`, `"de"`, or richer BCP-47 forms like
         * `"nl-BE"`). Only the language portion is considered.
         *
         * @return the matching entry, or `null` when [tag] is `null`,
         *         blank, or does not correspond to a supported language.
         */
        fun fromTag(tag: String?): SupportedLocale? {
            if (tag.isNullOrBlank()) return null
            val normalized = tag.trim().substringBefore('-').substringBefore('_').lowercase()
            return entries.firstOrNull { it.tag == normalized }
        }
    }
}
