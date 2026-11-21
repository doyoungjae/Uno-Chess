package com.example.unochessprototype.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * 앱 전체에서 사용하는 색상.
 * 현재 선택된 보드 테마의 MaterialTheme 색상표를 따라간다.
 */
object UnoChessColors {
    val Background: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val Surface: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val SurfaceRaised: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceSoft: Color
        @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val Lime: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val LimeDark: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val Text: Color
        @Composable get() = MaterialTheme.colorScheme.onBackground
    val Muted: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val Border: Color
        @Composable get() = MaterialTheme.colorScheme.outline
    val Warning: Color
        @Composable get() = MaterialTheme.colorScheme.tertiary
    val Danger: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val Blue: Color
        @Composable get() = MaterialTheme.colorScheme.secondary
}

@Immutable
data class BoardPalette(
    val light: Color,
    val dark: Color,
    val border: Color,
    val label: String,
    val backTop: Color,
    val backBottom: Color,
    val backAccent: Color,
)

const val BOARD_THEME_COUNT = 10

/** 보드판 + 카드 뒷면 프리셋. 0이 기본값. */
fun boardPalette(theme: Int): BoardPalette = when (theme) {
    4 -> BoardPalette(
        light = Color(0xFFF1F1EF), dark = Color(0xFF303236), border = Color(0xFF55585D),
        label = "모노크롬", backTop = Color(0xFF55585D), backBottom = Color(0xFF17191C),
        backAccent = Color(0xFFF1F1EF),
    )
    5 -> BoardPalette(
        light = Color(0xFFF1DCE4), dark = Color(0xFFA96F85), border = Color(0xFF754657),
        label = "로즈 라떼", backTop = Color(0xFFB77F94), backBottom = Color(0xFF633C4B),
        backAccent = Color(0xFFFFEAF0),
    )
    6 -> BoardPalette(
        light = Color(0xFFD9DDE2), dark = Color(0xFF626B76), border = Color(0xFF414850),
        label = "실버 그레이", backTop = Color(0xFF737D89), backBottom = Color(0xFF343A42),
        backAccent = Color(0xFFF0F2F5),
    )
    7 -> BoardPalette(
        light = Color(0xFFE8D7C5), dark = Color(0xFF80604A), border = Color(0xFF59402F),
        label = "모카", backTop = Color(0xFF987354), backBottom = Color(0xFF493323),
        backAccent = Color(0xFFF4E5D4),
    )
    8 -> BoardPalette(
        light = Color(0xFFDCECF5), dark = Color(0xFF648EAD), border = Color(0xFF42657E),
        label = "스카이", backTop = Color(0xFF6D9BB9), backBottom = Color(0xFF304F67),
        backAccent = Color(0xFFEAF7FF),
    )
    9 -> BoardPalette(
        light = Color(0xFFBBD9DF), dark = Color(0xFF285C70), border = Color(0xFF193E4C),
        label = "딥 오션", backTop = Color(0xFF326E83), backBottom = Color(0xFF142F3B),
        backAccent = Color(0xFFD8F0F2),
    )
    1 -> BoardPalette(
        light = Color(0xFFF0D9B5),
        dark = Color(0xFF8B5A3C),
        border = Color(0xFF4A2C21),
        label = "클래식 우드",
        backTop = Color(0xFF8A5631),
        backBottom = Color(0xFF3B2213),
        backAccent = Color(0xFFF0CF96),
    )
    2 -> BoardPalette(
        light = Color(0xFF8FA3C9),
        dark = Color(0xFF3A4470),
        border = Color(0xFF00E5FF),
        label = "다크 네온",
        backTop = Color(0xFF2A0F4A),
        backBottom = Color(0xFF07020F),
        backAccent = Color(0xFFFF3CAC),
    )
    3 -> BoardPalette(
        light = Color(0xFFFBE7EF),
        dark = Color(0xFFB9A7E0),
        border = Color(0xFF7E6BB0),
        label = "파스텔",
        backTop = Color(0xFFF7B2C8),
        backBottom = Color(0xFFA9C7F5),
        backAccent = Color(0xFFFFFFFF),
    )
else -> BoardPalette(
        light = Color(0xFFC7D9A7),
        dark = Color(0xFF41634A),
        border = Color(0xFF26382D),
        label = "포레스트",
        backTop = Color(0xFF173E28),
        backBottom = Color(0xFF091C12),
        backAccent = Color(0xFFA8E85A),
    )
}

/** 보드 테마 선택에 맞춰 앱의 배경, 카드, 글자, 버튼 색을 함께 변경한다. */
private fun appColorScheme(theme: Int) = when (theme) {
    4 -> darkColorScheme(
        primary = Color(0xFF42464B), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD9DADD), onPrimaryContainer = Color(0xFF202226),
        secondary = Color(0xFF777D84), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1E3E5), onSecondaryContainer = Color(0xFF292D32),
        tertiary = Color(0xFF666B70), background = Color(0xFFF1F1EF),
        onBackground = Color(0xFF25272A), surface = Color(0xFFFAFAF9),
        onSurface = Color(0xFF25272A), surfaceVariant = Color(0xFFE5E5E3),
        onSurfaceVariant = Color(0xFF5C5F63), outline = Color(0xFFB8B9BA),
        error = Color(0xFFBA1A1A),
    )
    5 -> darkColorScheme(
        primary = Color(0xFFA96F85), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF1DCE4), onPrimaryContainer = Color(0xFF4E2938),
        secondary = Color(0xFFB77F94), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF4E3E9), onSecondaryContainer = Color(0xFF593746),
        tertiary = Color(0xFF9B6B7D), background = Color(0xFFFAF3F5),
        onBackground = Color(0xFF382832), surface = Color(0xFFFFFAFC),
        onSurface = Color(0xFF382832), surfaceVariant = Color(0xFFF0E4E9),
        onSurfaceVariant = Color(0xFF78636D), outline = Color(0xFFD9BBC7),
        error = Color(0xFFBA1A1A),
    )
    6 -> darkColorScheme(
        primary = Color(0xFF626B76), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD9DDE2), onPrimaryContainer = Color(0xFF303943),
        secondary = Color(0xFF87919C), onSecondary = Color(0xFF17202A),
        secondaryContainer = Color(0xFFE8EBEF), onSecondaryContainer = Color(0xFF39434E),
        tertiary = Color(0xFF667F91), background = Color(0xFFF3F5F7),
        onBackground = Color(0xFF29313A), surface = Color(0xFFFCFDFE),
        onSurface = Color(0xFF29313A), surfaceVariant = Color(0xFFE7EAED),
        onSurfaceVariant = Color(0xFF626B74), outline = Color(0xFFC6CCD2),
        error = Color(0xFFBA1A1A),
    )
    7 -> darkColorScheme(
        primary = Color(0xFF80604A), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE8D7C5), onPrimaryContainer = Color(0xFF493323),
        secondary = Color(0xFFA17D5D), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF0E3D5), onSecondaryContainer = Color(0xFF543D2B),
        tertiary = Color(0xFF987354), background = Color(0xFFF8F3ED),
        onBackground = Color(0xFF382D25), surface = Color(0xFFFFFBF6),
        onSurface = Color(0xFF382D25), surfaceVariant = Color(0xFFEFE5DA),
        onSurfaceVariant = Color(0xFF746457), outline = Color(0xFFD8C5B2),
        error = Color(0xFFBA1A1A),
    )
    8 -> darkColorScheme(
        primary = Color(0xFF648EAD), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCECF5), onPrimaryContainer = Color(0xFF233F52),
        secondary = Color(0xFF7BA9C5), onSecondary = Color(0xFF102F42),
        secondaryContainer = Color(0xFFE4F1F8), onSecondaryContainer = Color(0xFF2D4B5D),
        tertiary = Color(0xFF628D9A), background = Color(0xFFF2F8FB),
        onBackground = Color(0xFF263640), surface = Color(0xFFFAFDFF),
        onSurface = Color(0xFF263640), surfaceVariant = Color(0xFFE5EFF4),
        onSurfaceVariant = Color(0xFF5E707A), outline = Color(0xFFC1D3DE),
        error = Color(0xFFBA1A1A),
    )
    9 -> darkColorScheme(
        primary = Color(0xFF285C70), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFBBD9DF), onPrimaryContainer = Color(0xFF153B49),
        secondary = Color(0xFF4B8496), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD9EBEF), onSecondaryContainer = Color(0xFF234A58),
        tertiary = Color(0xFF4F7D83), background = Color(0xFFF0F7F8),
        onBackground = Color(0xFF20363D), surface = Color(0xFFFAFEFF),
        onSurface = Color(0xFF20363D), surfaceVariant = Color(0xFFE1ECEE),
        onSurfaceVariant = Color(0xFF587078), outline = Color(0xFFB9D0D6),
        error = Color(0xFFBA1A1A),
    )
    1 -> darkColorScheme(
        primary = Color(0xFFD6A66B),
        onPrimary = Color(0xFF211307),
        primaryContainer = Color(0xFF5B3B20),
        onPrimaryContainer = Color(0xFFF5DFC1),
        secondary = Color(0xFFC78B5B),
        onSecondary = Color(0xFF241508),
        secondaryContainer = Color(0xFF49301F),
        onSecondaryContainer = Color(0xFFF1D5B8),
        tertiary = Color(0xFFE7C46E),
        background = Color(0xFF1B120D),
        onBackground = Color(0xFFF7EBDD),
        surface = Color(0xFF281B13),
        onSurface = Color(0xFFF7EBDD),
        surfaceVariant = Color(0xFF3A281C),
        onSurfaceVariant = Color(0xFFCBB9A7),
        outline = Color(0xFF6B4B34),
        error = Color(0xFFF28B82),
    )
    2 -> darkColorScheme(
        primary = Color(0xFFFF3CAC),
        onPrimary = Color(0xFF190518),
        primaryContainer = Color(0xFF4A1450),
        onPrimaryContainer = Color(0xFFFFD5F2),
        secondary = Color(0xFF00DDF5),
        onSecondary = Color(0xFF00191D),
        secondaryContainer = Color(0xFF123B49),
        onSecondaryContainer = Color(0xFFC7F7FF),
        tertiary = Color(0xFFF9C74F),
        background = Color(0xFF0C0715),
        onBackground = Color(0xFFF4E9FF),
        surface = Color(0xFF170D24),
        onSurface = Color(0xFFF4E9FF),
        surfaceVariant = Color(0xFF26143A),
        onSurfaceVariant = Color(0xFFC6B4D8),
        outline = Color(0xFF59356C),
        error = Color(0xFFFF7A8B),
    )
    3 -> darkColorScheme(
        primary = Color(0xFF9673C5),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE8DDF5),
        onPrimaryContainer = Color(0xFF3F3154),
        secondary = Color(0xFFD58EAF),
        onSecondary = Color(0xFF3B1E2D),
        secondaryContainer = Color(0xFFF0DDE9),
        onSecondaryContainer = Color(0xFF54364A),
        tertiary = Color(0xFFB9824A),
        background = Color(0xFFF8F2FA),
        onBackground = Color(0xFF332A3B),
        surface = Color(0xFFFFF9FF),
        onSurface = Color(0xFF332A3B),
        surfaceVariant = Color(0xFFF0E7F5),
        onSurfaceVariant = Color(0xFF74677E),
        outline = Color(0xFFD8C9E1),
        error = Color(0xFFC44C65),
    )
else -> darkColorScheme(
        primary = Color(0xFFA8E85A),
        onPrimary = Color(0xFF0B160D),
        primaryContainer = Color(0xFF28461D),
        onPrimaryContainer = Color(0xFFDFFFC2),
        secondary = Color(0xFF6DA8FF),
        onSecondary = Color(0xFF071A35),
        secondaryContainer = Color(0xFF203B5F),
        onSecondaryContainer = Color(0xFFD7E7FF),
        tertiary = Color(0xFFF2C94C),
        background = Color(0xFF08140D),
        onBackground = Color(0xFFF4F7F2),
        surface = Color(0xFF102219),
        onSurface = Color(0xFFF4F7F2),
        surfaceVariant = Color(0xFF173126),
        onSurfaceVariant = Color(0xFFAAB8AE),
        outline = Color(0xFF2D4A38),
        error = Color(0xFFE56D6D),
    )
}

@Composable
fun UnoChessTheme(
    boardTheme: Int = 0,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = appColorScheme(boardTheme),
        content = content,
    )
}
