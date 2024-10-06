package com.kartiknayak.easeexpense

import android.text.InputFilter
import android.text.Spanned

class EmojiInputFilter : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int,
    ): CharSequence? {
        for (index in start until end) {
            val type = Character.getType(source[index])
            if (type != Character.SURROGATE.toInt() && type != Character.NON_SPACING_MARK.toInt() && type != Character.OTHER_SYMBOL.toInt()) {
                return ""
            }
        }
        return null
    }
}
