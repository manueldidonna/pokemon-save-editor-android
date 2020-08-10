package com.manueldidonna.pk.utils

private const val G1TradeOT = '*'

fun getStringFromGameBoyData(
    data: UByteArray,
    startOffset: Int,
    stringLength: Int,
    isJapanese: Boolean = false
): String = StringBuilder().apply {
    val dictionary: Map<Int, Char> =
        if (!isJapanese) GameCharactersToInternational else throw IllegalStateException()
    for (i in 0 until stringLength) {
        val c: Char = dictionary[data[startOffset + i].toInt()] ?: break
        append(c.sanitize())
    }
}.toString()

fun getGameBoyDataFromString(
    value: String,
    maxValueLength: Int,
    isJapanese: Boolean = false,
    outputDataSize: Int,
    ignoreCase: Boolean
): UByteArray {
    require(outputDataSize > maxValueLength) {
        " String length must be equal or lower of the output data size"
    }

    val dictionary: Map<Char, Int> =
        if (!isJapanese) InternationalToGameCharacters else throw IllegalStateException()

    if (value.startsWith(G1TradeOT)) // handle TRAINER
        return ubyteArrayOf(dictionary[G1TradeOT]!!.toUByte(), 0x50u)

    val data = UByteArray(outputDataSize)
    var dataIndex = 0
    val realValue = if (ignoreCase) value.toUpperCase() else value
    for (i in 0 until realValue.length.coerceAtMost(maxValueLength)) {
        val b = dictionary[realValue[i]] ?: break
        data[dataIndex++] = b.toUByte()
    }
    data[dataIndex] = 0x50u
    return data
}

private fun Char.sanitize() = when (this) {
    '’' -> '\'' // farfetch'd
    '\uE08F' -> '♀' // ♀ (gen6+)
    '\uE08E' -> '♂' // ♂ (gen6+)
    '\u246E' -> '♀' // ♀ (gen5)
    '\u246D' -> '♂' // ♂ (gen5)
    else -> this
}

private val GameCharactersToInternational: Map<Int, Char> = mapOf(
    // 0x50 to G1Terminator,Dictionary
    0x5D to G1TradeOT,
    0x7F to ' ',
    0x80 to 'A',
    0x81 to 'B',
    0x82 to 'C',
    0x83 to 'D',
    0x84 to 'E',
    0x85 to 'F',
    0x86 to 'G',
    0x87 to 'H',
    0x88 to 'I',
    0x89 to 'J',
    0x8A to 'K',
    0x8B to 'L',
    0x8C to 'M',
    0x8D to 'N',
    0x8E to 'O',
    0x8F to 'P',
    0x90 to 'Q',
    0x91 to 'R',
    0x92 to 'S',
    0x93 to 'T',
    0x94 to 'U',
    0x95 to 'V',
    0x96 to 'W',
    0x97 to 'X',
    0x98 to 'Y',
    0x99 to 'Z',
    0x9A to '(',
    0x9B to ')',
    0x9C to ':',
    0x9D to ';',
    0x9E to '[',
    0x9F to ']',
    0xA0 to 'a',
    0xA1 to 'b',
    0xA2 to 'c',
    0xA3 to 'd',
    0xA4 to 'e',
    0xA5 to 'f',
    0xA6 to 'g',
    0xA7 to 'h',
    0xA8 to 'i',
    0xA9 to 'j',
    0xAA to 'k',
    0xAB to 'l',
    0xAC to 'm',
    0xAD to 'n',
    0xAE to 'o',
    0xAF to 'p',
    0xB0 to 'q',
    0xB1 to 'r',
    0xB2 to 's',
    0xB3 to 't',
    0xB4 to 'u',
    0xB5 to 'v',
    0xB6 to 'w',
    0xB7 to 'x',
    0xB8 to 'y',
    0xB9 to 'z',
    0xC0 to 'Ä',
    0xC1 to 'Ö',
    0xC2 to 'Ü',
    0xC3 to 'ä',
    0xC4 to 'ö',
    0xC5 to 'ü',
    0xE0 to '’',
    0xE1 to '{', /* Pk */
    0xE2 to '}', /* Mn */
    0xE3 to '-',
    0xE6 to '?',
    0xE7 to '!',
    0xE8 to '.', // Alias decimal point to  .
    0xEF to '♂',
    0xF1 to '×',
    0xF2 to '.',
    0xF3 to '/',
    0xF4 to ',',
    0xF5 to '♀',
    0xF6 to '0',
    0xF7 to '1',
    0xF8 to '2',
    0xF9 to '3',
    0xFA to '4',
    0xFB to '5',
    0xFC to '6',
    0xFD to '7',
    0xFE to '8',
    0xFF to '9'
)

private val InternationalToGameCharacters: Map<Char, Int> = mapOf(
    G1TradeOT to 0x5D, // TRAINER (Localized per ROM)
    ' ' to 0x7F,
    'A' to 0x80,
    'B' to 0x81,
    'C' to 0x82,
    'D' to 0x83,
    'E' to 0x84,
    'F' to 0x85,
    'G' to 0x86,
    'H' to 0x87,
    'I' to 0x88,
    'J' to 0x89,
    'K' to 0x8A,
    'L' to 0x8B,
    'M' to 0x8C,
    'N' to 0x8D,
    'O' to 0x8E,
    'P' to 0x8F,
    'Q' to 0x90,
    'R' to 0x91,
    'S' to 0x92,
    'T' to 0x93,
    'U' to 0x94,
    'V' to 0x95,
    'W' to 0x96,
    'X' to 0x97,
    'Y' to 0x98,
    'Z' to 0x99,
    '(' to 0x9A,
    ')' to 0x9B,
    ':' to 0x9C,
    ';' to 0x9D,
    '[' to 0x9E,
    ']' to 0x9F,
    'a' to 0xA0,
    'b' to 0xA1,
    'c' to 0xA2,
    'd' to 0xA3,
    'e' to 0xA4,
    'f' to 0xA5,
    'g' to 0xA6,
    'h' to 0xA7,
    'i' to 0xA8,
    'j' to 0xA9,
    'k' to 0xAA,
    'l' to 0xAB,
    'm' to 0xAC,
    'n' to 0xAD,
    'o' to 0xAE,
    'p' to 0xAF,
    'q' to 0xB0,
    'r' to 0xB1,
    's' to 0xB2,
    't' to 0xB3,
    'u' to 0xB4,
    'v' to 0xB5,
    'w' to 0xB6,
    'x' to 0xB7,
    'y' to 0xB8,
    'z' to 0xB9,

    // unused characters
    'à' to 0xBA,
    'è' to 0xBB,
    'é' to 0xBC,
    'ù' to 0xBD,
    'À' to 0xBE,
    'Á' to 0xBF, // Used in Spanish FALCÁN in-game trade, inaccessible from keyboard

    'Ä' to 0xC0,
    'Ö' to 0xC1,
    'Ü' to 0xC2,
    'ä' to 0xC3,
    'ö' to 0xC4,
    'ü' to 0xC5,

    // unused characters
    'È' to 0xC6,
    'É' to 0xC7,
    'Ì' to 0xC8,
    'Í' to 0xC9, // Used in Spanish MANÍA in-game trade, inaccessible from keyboard
    'Ñ' to 0xCA,
    'Ò' to 0xCB,
    'Ó' to 0xCC,
    'Ù' to 0xCD,
    'Ú' to 0xCE,
    'á' to 0xCF,
    'ì' to 0xD0,
    'í' to 0xD1,
    'ñ' to 0xD2,
    'ò' to 0xD3,
    'ó' to 0xD4,
    'ú' to 0xD5,

    '\'' to 0xE0, // Alias ' to ’ for Farfetch'd
    '’' to 0xE0,
    '{' to 0xE1, /* Pk */
    '}' to 0xE2, /* Mn */
    '-' to 0xE3,
    '?' to 0xE6,
    '!' to 0xE7,
    '♂' to 0xEF,
    '×' to 0xF1,
    '.' to 0xF2,
    '/' to 0xF3,
    ',' to 0xF4,
    '♀' to 0xF5,
    '0' to 0xF6,
    '1' to 0xF7,
    '2' to 0xF8,
    '3' to 0xF9,
    '4' to 0xFA,
    '5' to 0xFB,
    '6' to 0xFC,
    '7' to 0xFD,
    '8' to 0xFE,
    '9' to 0xFF
)
