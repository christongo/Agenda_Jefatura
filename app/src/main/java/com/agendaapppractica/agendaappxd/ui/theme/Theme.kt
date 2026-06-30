package com.agendaapppractica.agendaappxd.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AgendaappxdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTema: String = "Morado",
    content: @Composable () -> Unit
) {

    val colorScheme = when (colorTema) {

        "Azul" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFF64B5F6),
                    secondary = Color(0xFF42A5F5),
                    tertiary = Color(0xFF90CAF9),
                    background = Color(0xFF10141D),
                    surface = Color(0xFF161B26),
                    surfaceVariant = Color(0xFF21293A)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF1976D2),
                    secondary = Color(0xFF2196F3),
                    tertiary = Color(0xFF64B5F6),
                    background = Color(0xFFF0F4F8),
                    surface = Color(0xFFF7FAFC),
                    surfaceVariant = Color(0xFFE2E8F0)
                )
            }
        }

        "Verde" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFF66BB6A),
                    secondary = Color(0xFF81C784),
                    tertiary = Color(0xFFA5D6A7),
                    background = Color(0xFF0F1410),
                    surface = Color(0xFF151C17),
                    surfaceVariant = Color(0xFF1E2820)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF2E7D32),
                    secondary = Color(0xFF4CAF50),
                    tertiary = Color(0xFF81C784),
                    background = Color(0xFFF1F7F2),
                    surface = Color(0xFFF8FBF9),
                    surfaceVariant = Color(0xFFE3EFE5)
                )
            }
        }

        "Rojo" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFEF5350),
                    secondary = Color(0xFFE57373),
                    tertiary = Color(0xFFFFCDD2),
                    background = Color(0xFF161010),
                    surface = Color(0xFF1F1515),
                    surfaceVariant = Color(0xFF2D1E1E)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFC62828),
                    secondary = Color(0xFFE53935),
                    tertiary = Color(0xFFEF5350),
                    background = Color(0xFFFAF2F2),
                    surface = Color(0xFFFDF8F8),
                    surfaceVariant = Color(0xFFF5E4E4)
                )
            }
        }

        "Naranja" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFFFB74D),
                    secondary = Color(0xFFFFA726),
                    tertiary = Color(0xFFFFCC80),
                    background = Color(0xFF14110F),
                    surface = Color(0xFF1B1714),
                    surfaceVariant = Color(0xFF27211C)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFEF6C00),
                    secondary = Color(0xFFFF9800),
                    tertiary = Color(0xFFFFB74D),
                    background = Color(0xFFFAF5F0),
                    surface = Color(0xFFFDFBF7),
                    surfaceVariant = Color(0xFFF5EAE0)
                )
            }
        }

        // 🔥 NUEVO COLOR: ROSA
        "Rosa" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFF06292),
                    secondary = Color(0xFFEC407A),
                    tertiary = Color(0xFFF8BBD0),
                    background = Color(0xFF171012),
                    surface = Color(0xFF211518),
                    surfaceVariant = Color(0xFF2F1E22)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFC2185B),
                    secondary = Color(0xFFE91E63),
                    tertiary = Color(0xFFF06292),
                    background = Color(0xFFFDF2F5),
                    surface = Color(0xFFFFF7F9),
                    surfaceVariant = Color(0xFFF6E4EB)
                )
            }
        }

        // 🔥 NUEVO COLOR: CIAN
        "Cian" -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFF4DD0E1),
                    secondary = Color(0xFF26C6DA),
                    tertiary = Color(0xFFB2EBF2),
                    background = Color(0xFF0F1617),
                    surface = Color(0xFF141E20),
                    surfaceVariant = Color(0xFF1D2B2E)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF0097A7),
                    secondary = Color(0xFF00BCD4),
                    tertiary = Color(0xFF4DD0E1),
                    background = Color(0xFFF0F9FA),
                    surface = Color(0xFFF6FCFD),
                    surfaceVariant = Color(0xFFE0F2F4)
                )
            }
        }

        else -> { // Morado
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFBA68C8),
                    secondary = Color(0xFFCE93D8),
                    tertiary = Color(0xFFE1BEE7),
                    background = Color(0xFF121014),
                    surface = Color(0xFF1A161E),
                    surfaceVariant = Color(0xFF251F2A)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF7B1FA2),
                    secondary = Color(0xFF9C27B0),
                    tertiary = Color(0xFFBA68C8),
                    background = Color(0xFFF6F3F8),
                    surface = Color(0xFFFAF8FB),
                    surfaceVariant = Color(0xFFEFEAF2)
                )
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}