package com.manueldidonna.pk.utils

/**
 * Return true (1) or false (0)
 */
fun Int.getBitAt(index: Int): Boolean {
    return (this ushr index and 1) != 0
}
