package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix

data class InstagramFilter(
    val name: String,
    val description: String,
    val colorMatrix: ColorMatrix,
    val previewTint: Color
)

object InstagramFilters {
    val ALL = listOf(
        InstagramFilter(
            name = "Normal",
            description = "Natural original",
            colorMatrix = ColorMatrix(),
            previewTint = Color.White
        ),
        InstagramFilter(
            name = "Clarendon",
            description = "Vibrant punch & cyan boost",
            colorMatrix = ColorMatrix(
                floatArrayOf(
                    1.2f, 0f, 0f, 0f, 5f,
                    0f, 1.15f, 0f, 0f, 5f,
                    0f, 0f, 1.35f, 0f, 12f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            previewTint = Color(0xFF64B5F6)
        ),
        InstagramFilter(
            name = "Juno",
            description = "Warm golden sunset glow",
            colorMatrix = ColorMatrix(
                floatArrayOf(
                    1.3f, 0f, 0f, 0f, 15f,
                    0f, 1.12f, 0f, 0f, 8f,
                    0f, 0f, 0.95f, 0f, -6f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            previewTint = Color(0xFFFFB74D)
        ),
        InstagramFilter(
            name = "Valencia",
            description = "Warm vintage film look",
            colorMatrix = ColorMatrix(
                floatArrayOf(
                    1.12f, 0.08f, 0.05f, 0f, 18f,
                    0f, 1.08f, 0.04f, 0f, 12f,
                    0f, 0f, 0.92f, 0f, 6f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            previewTint = Color(0xFFFFCC80)
        ),
        InstagramFilter(
            name = "Moon",
            description = "Deep moody black & white",
            colorMatrix = ColorMatrix(
                floatArrayOf(
                    0.299f * 1.35f, 0.587f * 1.35f, 0.114f * 1.35f, 0f, -12f,
                    0.299f * 1.35f, 0.587f * 1.35f, 0.114f * 1.35f, 0f, -12f,
                    0.299f * 1.35f, 0.587f * 1.35f, 0.114f * 1.35f, 0f, -12f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            previewTint = Color(0xFFB0BEC5)
        ),
        InstagramFilter(
            name = "Lark",
            description = "Bright, airy & vivid skies",
            colorMatrix = ColorMatrix(
                floatArrayOf(
                    1.08f, 0f, 0f, 0f, 14f,
                    0f, 1.18f, 0f, 0f, 16f,
                    0f, 0f, 1.25f, 0f, 20f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            previewTint = Color(0xFF81D4FA)
        ),
        InstagramFilter(
            name = "Lo-Fi",
            description = "High contrast rich saturation",
            colorMatrix = ColorMatrix(
                floatArrayOf(
                    1.38f, 0f, 0f, 0f, -18f,
                    0f, 1.34f, 0f, 0f, -18f,
                    0f, 0f, 1.28f, 0f, -18f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            previewTint = Color(0xFFCE93D8)
        ),
        InstagramFilter(
            name = "Slumber",
            description = "Muted retro dreamy film",
            colorMatrix = ColorMatrix(
                floatArrayOf(
                    0.96f, 0.04f, 0f, 0f, 12f,
                    0f, 0.92f, 0.04f, 0f, 10f,
                    0f, 0f, 0.88f, 0f, 14f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            previewTint = Color(0xFFA5D6A7)
        ),
        InstagramFilter(
            name = "Reyes",
            description = "Dusty warm nostalgic pastel",
            colorMatrix = ColorMatrix(
                floatArrayOf(
                    1.14f, 0.06f, 0.06f, 0f, 22f,
                    0.06f, 1.08f, 0.06f, 0f, 22f,
                    0.06f, 0.06f, 1.02f, 0f, 22f,
                    0f, 0f, 0f, 1f, 0f
                )
            ),
            previewTint = Color(0xFFFFAB91)
        )
    )

    fun getFilter(name: String): InstagramFilter {
        return ALL.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: ALL[0]
    }
}
