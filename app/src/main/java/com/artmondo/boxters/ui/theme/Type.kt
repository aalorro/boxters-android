package com.artmondo.boxters.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.artmondo.boxters.R

val CinzelFontFamily = FontFamily(
    Font(R.font.cinzel_regular, FontWeight.Normal),
    Font(R.font.cinzel_bold, FontWeight.Bold),
    Font(R.font.cinzel_black, FontWeight.Black),
)

val InterFontFamily = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

object GameTypography {
    val title get() = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 42.sp,
        color = GameColors.uiAccent
    )
    val levelName get() = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        color = GameColors.uiAccent
    )
    val modeBadge get() = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    )
    val scoreMain get() = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        color = GameColors.uiAccent
    )
    val scoreLevel get() = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = GameColors.uiTextDim
    )
    val wordTray get() = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp
    )
    val tutorial get() = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        color = GameColors.uiText
    )
    val objective get() = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        color = GameColors.uiText
    )
    val button get() = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp
    )
    val body get() = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        color = GameColors.uiText
    )
    val statValue get() = TextStyle(
        fontFamily = CinzelFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        color = GameColors.uiAccent
    )
    val statLabel get() = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = GameColors.uiTextDim
    )
}
