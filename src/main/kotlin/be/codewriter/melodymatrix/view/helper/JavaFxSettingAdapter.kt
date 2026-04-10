package be.codewriter.melodymatrix.view.helper

/**
 * Bridges any [SettingStorage] into a full [SettingHelper].
 *
 * All storage calls are delegated to [storage] via Kotlin's `by` keyword.
 * The JavaFX bind/color methods come for free from [SettingHelper]'s default
 * implementations — no JavaFX code lives in the Engine.
 *
 * Usage — inject from Engine or MainApplication:
 *   val settings = JavaFxSettingAdapter(engineSettingService)
 *   ChordRelationView(settings)
 */
class JavaFxSettingAdapter(storage: SettingStorage) : SettingHelper, SettingStorage by storage
