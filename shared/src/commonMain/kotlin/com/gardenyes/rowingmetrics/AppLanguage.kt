package com.gardenyes.rowingmetrics

/**
 * User-selectable UI language. [ENGLISH] is the default.
 * [localeTag] is passed to [com.gardenyes.rowingmetrics.ui.LocalAppLocale] for compose-resources qualifiers.
 */
enum class AppLanguage(val localeTag: String) {
    ENGLISH("en"),
    CATALAN("ca"),
    SPANISH("es"),
    FRENCH("fr"),
    ;

    companion object {
        val DEFAULT = ENGLISH

        fun fromStored(value: String?): AppLanguage =
            entries.find { it.name == value } ?: DEFAULT
    }
}
