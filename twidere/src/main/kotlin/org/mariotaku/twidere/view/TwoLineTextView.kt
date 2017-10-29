/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.support.annotation.Dimension
import android.support.annotation.Px
import android.support.v4.text.BidiFormatter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue
import org.mariotaku.twidere.R

open class TwoLineTextView(context: Context, attrs: AttributeSet? = null) : FixedTextView(context, attrs) {

    var twoLine: Boolean = false
        set(value) {
            field = value
            maxLines = if (value) {
                2
            } else {
                1
            }
        }

    var primaryTextStyle: Int
        get() = primaryTextStyleSpan?.style ?: 0
        set(value) {
            primaryTextStyleSpan = StyleSpan(value)
        }

    var secondaryTextStyle: Int
        get() = secondaryTextStyleSpan?.style ?: 0
        set(value) {
            secondaryTextStyleSpan = StyleSpan(value)
        }

    var primaryTextSize: Int
        @Px get() = primaryTextSizeSpan?.size ?: 0
        set(@Px value) {
            primaryTextSizeSpan = AbsoluteSizeSpan(value)
        }

    var secondaryTextSize: Int
        @Px get() = secondaryTextSizeSpan?.size ?: 0
        set(@Px value) {
            secondaryTextSizeSpan = AbsoluteSizeSpan(value)
        }

    var primaryTextColor: Int
        @ColorInt get() = primaryTextColorSpan?.foregroundColor ?: 0
        set(@ColorInt value) {
            primaryTextColorSpan = ForegroundColorSpan(value)
        }

    var secondaryTextColor: Int
        @ColorInt get() = secondaryTextColorSpan?.foregroundColor ?: 0
        set(@ColorInt value) {
            secondaryTextColorSpan = ForegroundColorSpan(value)
        }

    var primaryText: CharSequence? = null
    var secondaryText: CharSequence? = null

    protected open val displayPrimaryText: CharSequence?
        get() = primaryText

    protected open val displaySecondaryText: CharSequence?
        get() = secondaryText

    private var primaryTextStyleSpan: StyleSpan? = null
    private var secondaryTextStyleSpan: StyleSpan? = null

    private var primaryTextColorSpan: ForegroundColorSpan? = null
    private var secondaryTextColorSpan: ForegroundColorSpan? = null
    private var primaryTextSizeSpan: AbsoluteSizeSpan? = null
    private var secondaryTextSizeSpan: AbsoluteSizeSpan? = null


    init {
        ellipsize = TextUtils.TruncateAt.END
        val a = context.obtainStyledAttributes(attrs, R.styleable.TwoLineTextView, 0, 0)
        twoLine = a.getBoolean(R.styleable.TwoLineTextView_tltvTwoLine, false)
        primaryText = a.getText(R.styleable.TwoLineTextView_tltvPrimaryText)
        secondaryText = a.getText(R.styleable.TwoLineTextView_tltvSecondaryText)
        primaryTextColor = a.getColor(R.styleable.TwoLineTextView_tltvPrimaryTextColor, currentTextColor)
        secondaryTextColor = a.getColor(R.styleable.TwoLineTextView_tltvSecondaryTextColor, currentTextColor)
        primaryTextStyle = a.getInt(R.styleable.TwoLineTextView_tltvPrimaryTextStyle, Typeface.NORMAL)
        secondaryTextStyle = a.getInt(R.styleable.TwoLineTextView_tltvSecondaryTextStyle, Typeface.NORMAL)
        primaryTextSize = a.getDimensionPixelSize(R.styleable.TwoLineTextView_tltvPrimaryTextSize, textSize.toInt())
        secondaryTextSize = a.getDimensionPixelSize(R.styleable.TwoLineTextView_tltvSecondaryTextSize, textSize.toInt())
        a.recycle()
        updateText()
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        return try {
            super.onTextContextMenuItem(id)
        } catch (e: AbstractMethodError) {
            // http://crashes.to/s/69acd0ea0de
            true
        }
    }

    fun updateText(formatter: BidiFormatter? = null) {
        val sb = SpannableStringBuilder()
        val primaryText = displayPrimaryText
        val secondaryText = displaySecondaryText
        if (primaryText != null) {
            val start = sb.length
            if (formatter != null && !isInEditMode) {
                sb.append(formatter.unicodeWrap(primaryText))
            } else {
                sb.append(primaryText)
            }
            val end = sb.length
            sb.setSpan(primaryTextColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(primaryTextStyleSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(primaryTextSizeSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        sb.append(if (twoLine) '\n' else ' ')
        if (secondaryText != null) {
            val start = sb.length
            if (formatter != null && !isInEditMode) {
                sb.append(formatter.unicodeWrap(secondaryText))
            } else {
                sb.append(secondaryText)
            }
            val end = sb.length
            sb.setSpan(secondaryTextColorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(secondaryTextStyleSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            sb.setSpan(secondaryTextSizeSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        setText(sb, BufferType.SPANNABLE)
    }

    fun setPrimaryTextSize(size: Float, unit: Int = TypedValue.COMPLEX_UNIT_SP) {
        primaryTextSize = calculateTextSize(size, unit).toInt()
    }

    fun setSecondaryTextSize(size: Float, unit: Int = TypedValue.COMPLEX_UNIT_SP) {
        secondaryTextSize = calculateTextSize(size, unit).toInt()
    }

    @Dimension(unit = Dimension.PX)
    private fun calculateTextSize(size: Float, unit: Int): Float {
        val r = context.resources ?: Resources.getSystem()
        return TypedValue.applyDimension(unit, size, r.displayMetrics)
    }

}