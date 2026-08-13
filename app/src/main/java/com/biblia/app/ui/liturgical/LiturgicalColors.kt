package com.biblia.app.ui.liturgical

import androidx.compose.ui.graphics.Color
import com.biblia.app.data.liturgical.LiturgicalColor

/** Traditional liturgical colors, used for calendar day-tinting and rank badges. */
object LiturgicalColors {
    val Violet = Color(0xFF6A4C93)
    val White = Color(0xFFB8A369)
    val Green = Color(0xFF2E7D32)
    val Red = Color(0xFFC62828)
    val Rose = Color(0xFFD98BA0)
    val Black = Color(0xFF3A3A3A)

    fun fromApiName(name: String): Color = when (name.lowercase()) {
        "purple", "violet" -> Violet
        "white" -> White
        "green" -> Green
        "red" -> Red
        "pink", "rose" -> Rose
        "black" -> Black
        else -> Green
    }

    fun fromOffline(color: LiturgicalColor): Color = when (color) {
        LiturgicalColor.VIOLET -> Violet
        LiturgicalColor.WHITE -> White
        LiturgicalColor.GREEN -> Green
        LiturgicalColor.RED -> Red
        LiturgicalColor.ROSE -> Rose
    }
}
