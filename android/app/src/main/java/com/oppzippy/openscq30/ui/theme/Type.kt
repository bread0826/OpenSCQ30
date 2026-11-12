package com.oppzippy.openscq30.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.oppzippy.openscq30.R

private val defaultTypography = Typography()
private val headingFont = FontFamily(Font(R.font.doto))

internal val Typography = defaultTypography.copy(
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = headingFont,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.4.sp,
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = headingFont,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.4.sp,
    ),
)
