package com.example.unochessprototype.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.ui.theme.BOARD_THEME_COUNT
import com.example.unochessprototype.ui.theme.CardBackFace
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.ui.theme.boardPalette
import com.example.unochessprototype.ui.theme.deleteCustomImage
import com.example.unochessprototype.ui.theme.importPickedImage
import androidx.compose.foundation.Image
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import com.example.unochessprototype.data.CustomThemeSet
import com.example.unochessprototype.data.savedSetImagePaths
import com.example.unochessprototype.ui.theme.rememberLocalImage
import kotlinx.coroutines.launch

/** 보드 & 카드 커스텀 전용 화면. 설정은 이 기기에만 저장된다. */
@Composable
fun CustomizeScreen(
    settings: LocalSettings,
    onSettingsChange: (LocalSettings) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "뒤로")
                }
                Text("보드 & 카드 커스텀", fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
        }
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                MiniBoard(settings)
            }
        }
        item { CustomizeCard(settings, onSettingsChange) }
    }
}

@Composable
private fun CustomizeCard(settings: LocalSettings, onSettingsChange: (LocalSettings) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // 포토 피커 결과를 앱 내부로 복사해 경로를 저장한다(앱 재시작 후에도 유지, 이 기기에서만 사용).
    val pickBoard = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            importPickedImage(context, uri, "board", settings.boardImagePath.unlessSaved(settings))?.let {
                onSettingsChange(settings.copy(boardImagePath = it))
            }
        }
    }
    val pickCardBack = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            importPickedImage(context, uri, "cardback", settings.cardBackImagePath.unlessSaved(settings))?.let {
                onSettingsChange(settings.copy(cardBackImagePath = it))
            }
        }
    }
    val imageOnly = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    Card(
        colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
        shape = RoundedCornerShape(22.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("보드 & 카드 커스텀", fontWeight = FontWeight.Black, fontSize = 17.sp)
            Text("내 화면에만 적용됩니다.", color = UnoChessColors.Muted, fontSize = 11.sp)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (rowStart in 0 until BOARD_THEME_COUNT step 5) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(minOf(5, BOARD_THEME_COUNT - rowStart)) { column ->
                            val index = rowStart + column
                            ThemePresetChip(
                                settings = settings,
                                theme = index,
                                selected = settings.boardTheme == index,
                                modifier = Modifier.weight(1f),
                                onClick = { onSettingsChange(settings.copy(boardTheme = index)) },
                            )
                        }
                    }
                }
            }

            ImagePickRow(
                title = "보드판 사진",
                hasImage = settings.boardImagePath != null,
                onPick = { pickBoard.launch(imageOnly) },
                onClear = {
                    deleteCustomImage(settings.boardImagePath.unlessSaved(settings))
                    onSettingsChange(settings.copy(boardImagePath = null))
                },
            )
            ImagePickRow(
                title = "카드 뒷면 사진",
                hasImage = settings.cardBackImagePath != null,
                onPick = { pickCardBack.launch(imageOnly) },
                onClear = {
                    deleteCustomImage(settings.cardBackImagePath.unlessSaved(settings))
                    onSettingsChange(settings.copy(cardBackImagePath = null))
                },
                preview = { CardBackFace(settings, Modifier.width(30.dp).height(44.dp)) },
            )

            SaveThemeSetRow { name ->
                val set = CustomThemeSet(
                    id = System.currentTimeMillis(),
                    name = name,
                    baseTheme = settings.boardTheme,
                    boardImagePath = settings.boardImagePath,
                    cardBackImagePath = settings.cardBackImagePath,
                )
                onSettingsChange(settings.copy(themeSets = settings.themeSets + set))
            }

            if (settings.themeSets.isNotEmpty()) {
                Text("나만의 테마", fontWeight = FontWeight.Black, fontSize = 15.sp)
                settings.themeSets.forEach { set ->
                    ThemeSetCard(
                        settings = settings,
                        set = set,
                        selected = settings.boardTheme == set.baseTheme &&
                            settings.boardImagePath == set.boardImagePath &&
                            settings.cardBackImagePath == set.cardBackImagePath,
                        onApply = {
                            // shortcut: 적용 전 '저장 안 된' 현재 사진 파일은 지우지 않고 남겨 둔다(데이터 손실 방지). 용량이 문제되면 정리 로직 추가.
                            onSettingsChange(
                                settings.copy(
                                    boardTheme = set.baseTheme,
                                    boardImagePath = set.boardImagePath,
                                    cardBackImagePath = set.cardBackImagePath,
                                )
                            )
                        },
                        onDelete = {
                            val remaining = settings.themeSets - set
                            val inUse = settings.copy(themeSets = remaining).savedSetImagePaths() +
                                listOfNotNull(settings.boardImagePath, settings.cardBackImagePath)
                            listOfNotNull(set.boardImagePath, set.cardBackImagePath)
                                .filterNot { it in inUse }
                                .forEach(::deleteCustomImage)
                            onSettingsChange(settings.copy(themeSets = remaining))
                        },
                    )
                }
            }
        }
    }
}

/** 저장된 테마가 쓰는 사진이면 null(지우지 않음). */
private fun String?.unlessSaved(settings: LocalSettings): String? =
    this?.takeUnless { it in settings.savedSetImagePaths() }

@Composable
private fun SaveThemeSetRow(onSave: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(20) },
            modifier = Modifier.weight(1f),
            label = { Text("테마 이름 (예: 나만의 세트 A)") },
            singleLine = true,
        )
        Button(
            onClick = {
                onSave(name.trim())
                name = ""
            },
            enabled = name.isNotBlank(),
        ) { Text("저장") }
    }
}

@Composable
private fun ThemeSetCard(
    settings: LocalSettings,
    set: CustomThemeSet,
    selected: Boolean,
    onApply: () -> Unit,
    onDelete: () -> Unit,
) {
    val palette = boardPalette(set.baseTheme)
    val boardImage = rememberLocalImage(set.boardImagePath, maxSidePx = 256)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(UnoChessColors.SurfaceRaised, RoundedCornerShape(14.dp))
            .border(2.dp, if (selected) UnoChessColors.Lime else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(onClick = onApply)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))) {
            if (boardImage != null) {
                Image(boardImage, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Column {
                    repeat(2) { r ->
                        Row {
                            repeat(2) { c ->
                                Box(Modifier.size(22.dp).background(if ((r + c) % 2 == 0) palette.light else palette.dark))
                            }
                        }
                    }
                }
            }
        }
        CardBackFace(
            settings.copy(boardTheme = set.baseTheme, cardBackImagePath = set.cardBackImagePath),
            Modifier.width(30.dp).height(44.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(set.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (selected) UnoChessColors.Lime else UnoChessColors.Text)
            Text(
                listOf(
                    palette.label,
                    if (set.boardImagePath != null) "보드 사진" else null,
                    if (set.cardBackImagePath != null) "카드 사진" else null,
                ).filterNotNull().joinToString(" · "),
                color = UnoChessColors.Muted,
                fontSize = 10.sp,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Rounded.Delete, contentDescription = "${set.name} 삭제", tint = UnoChessColors.Muted)
        }
    }
}

@Composable
private fun ThemePresetChip(
    settings: LocalSettings,
    theme: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = boardPalette(theme)
    Column(
        modifier = modifier
            .background(UnoChessColors.SurfaceRaised, RoundedCornerShape(14.dp))
            .border(2.dp, if (selected) UnoChessColors.Lime else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column {
                repeat(2) { r ->
                    Row {
                        repeat(2) { c ->
                            Box(
                                Modifier.size(11.dp)
                                    .background(if ((r + c) % 2 == 0) palette.light else palette.dark)
                            )
                        }
                    }
                }
            }
            // 프리셋 미리보기는 사진을 빼고 테마 패턴만 보여준다.
            CardBackFace(settings.copy(boardTheme = theme, cardBackImagePath = null), Modifier.width(16.dp).height(23.dp))
        }
        Text(
            palette.label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = if (selected) UnoChessColors.Lime else UnoChessColors.Text,
        )
    }
}

@Composable
private fun ImagePickRow(
    title: String,
    hasImage: Boolean,
    onPick: () -> Unit,
    onClear: () -> Unit,
    preview: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        preview?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                if (hasImage) "내 사진 적용 중" else "테마 기본 디자인",
                color = if (hasImage) UnoChessColors.Lime else UnoChessColors.Muted,
                fontSize = 10.sp,
            )
        }
        OutlinedButton(onClick = onPick) {
            Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" 갤러리", fontSize = 12.sp)
        }
        if (hasImage) {
            IconButton(onClick = onClear) {
                Icon(Icons.Rounded.Close, contentDescription = "$title 제거", tint = UnoChessColors.Muted)
            }
        }
    }
}
