package ca.gbc.comp3074.p12.restaurantguide.data

import java.util.Locale

/** Optional numbers remain blank; each provided line must contain a plausible phone number. */
fun validPhoneNumbers(value: String): Boolean = value.split('\n', ';').filter { it.isNotBlank() }.all { number ->
    number.all { it.isDigit() || it in "+-(). " } && number.count(Char::isDigit) in 7..15
}

/** Canadian postal codes are consistently capitalized/spaced; other address text is retained. */
fun normalizeAddress(value: String): String = Regex("\\b([A-Za-z]\\d[A-Za-z])\\s?(\\d[A-Za-z]\\d)\\b").replace(value.trim()) {
    "${it.groupValues[1].uppercase(Locale.ROOT)} ${it.groupValues[2].uppercase(Locale.ROOT)}"
}
