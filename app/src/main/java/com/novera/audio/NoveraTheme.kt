package com.novera.audio

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class NoveraPalette(
    val midnight: Color,
    val deepPanel: Color,
    val raisedPanel: Color,
    val cyan: Color,
    val violet: Color,
    val softText: Color,
    val mutedText: Color
)

enum class NoveraTheme(
    val label: String,
    val description: String,
    val palette: NoveraPalette
) {
    AURORA(
        "Aurora",
        "Cian eléctrico y violeta nocturno",
        NoveraPalette(Color(0xFF070B16), Color(0xFF101827), Color(0xFF172238), Color(0xFF65E9FF), Color(0xFF9E7BFF), Color(0xFFAEBBD0), Color(0xFF6D7B91))
    ),
    OBSIDIAN(
        "Obsidian",
        "Negro profundo con azul hielo",
        NoveraPalette(Color(0xFF050607), Color(0xFF101315), Color(0xFF1D2528), Color(0xFFB7F0FF), Color(0xFF5C9CFF), Color(0xFFB8C6CC), Color(0xFF718087))
    ),
    NEBULA(
        "Nebula",
        "Azul cósmico y magenta suave",
        NoveraPalette(Color(0xFF0B0718), Color(0xFF19102A), Color(0xFF28183B), Color(0xFFFF8CD8), Color(0xFF8EA7FF), Color(0xFFD0C2DE), Color(0xFF8F7D9E))
    ),
    EMERALD(
        "Emerald",
        "Verde mineral con azul petróleo",
        NoveraPalette(Color(0xFF06120F), Color(0xFF0D211D), Color(0xFF14342E), Color(0xFF69F2C2), Color(0xFF53B8FF), Color(0xFFB3D7CD), Color(0xFF6D978A))
    ),
    COPPER(
        "Copper",
        "Cobre elegante con índigo oscuro",
        NoveraPalette(Color(0xFF110A0A), Color(0xFF211514), Color(0xFF34201D), Color(0xFFFFB27A), Color(0xFFB89CFF), Color(0xFFE0C5B7), Color(0xFFA17D6D))
    )
}

val LocalNoveraPalette = staticCompositionLocalOf { NoveraTheme.AURORA.palette }

class ThemeStore(context: Context) {
    private val prefs = context.getSharedPreferences("novera_preferences", Context.MODE_PRIVATE)

    fun load(): NoveraTheme = NoveraTheme.values().getOrElse(prefs.getInt("theme", 0)) { NoveraTheme.AURORA }

    fun save(theme: NoveraTheme) {
        prefs.edit().putInt("theme", theme.ordinal).apply()
    }
}
