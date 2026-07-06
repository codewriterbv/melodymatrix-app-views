package be.codewriter.melodymatrix.view.i18n

import java.util.Locale

/**
 * Enumerates the languages supported by MelodyMatrix.
 *
 * Each entry carries the resolved [Locale] used to look up `.properties`
 * bundles and a **native** [displayName] (rendered in the language's own
 * script) suitable for showing in the language selector combo box.
 *
 * `pt-BR` is used for Portuguese so translations target the Brazilian
 * variant (largest lusophone music-consumption market); `zh-CN` is used
 * for Chinese so translations target Simplified Chinese.
 */
enum class SupportedLocale(
    val locale: Locale,
    val displayName: String
) {
    EN(Locale.ENGLISH, "English"),
    NL(Locale.of("nl"), "Nederlands"),
    FR(Locale.FRENCH, "Français"),
    DE(Locale.GERMAN, "Deutsch"),
    ES(Locale.of("es"), "Español"),
    IT(Locale.ITALIAN, "Italiano"),
    PT(Locale.of("pt", "BR"), "Português (Brasil)"),
    JA(Locale.JAPANESE, "日本語"),
    ZH(Locale.of("zh", "CN"), "简体中文"),
    PL(Locale.of("pl"), "Polski");

    /** Short language tag ("en", "nl", "fr", "de", "es", "it", "pt", "ja", "zh", "pl"). */
    val tag: String get() = locale.language

    companion object {
        /**
         * Resolves the [SupportedLocale] matching the given language [tag]
         * (e.g. `"en"`, `"pt"`, `"zh"`, or richer BCP-47 forms like
         * `"pt-BR"` / `"zh-CN"`). Only the language portion is considered,
         * so `"pt-PT"` still resolves to Brazilian Portuguese — English
         * fallback is only for unsupported languages.
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
