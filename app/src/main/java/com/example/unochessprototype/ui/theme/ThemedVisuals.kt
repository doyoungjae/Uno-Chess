package com.example.unochessprototype.ui.theme

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unochessprototype.data.LocalSettings
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 앱 내부 저장소의 이미지 파일을 (축소해서) 읽는다. path가 null이거나 읽기 실패면 null. */
@Composable
fun rememberLocalImage(path: String?, maxSidePx: Int = 1024): ImageBitmap? {
    val image by produceState<ImageBitmap?>(initialValue = null, path) {
        value = if (path == null) null else withContext(Dispatchers.IO) { decodeScaled(path, maxSidePx) }
    }
    return image
}

private fun decodeScaled(path: String, maxSidePx: Int): ImageBitmap? = runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxSidePx) sample *= 2
    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
}.getOrNull()

/**
 * 포토 피커가 준 Uri의 이미지를 앱 내부 저장소로 복사하고 경로를 돌려준다.
 * 피커 Uri 권한은 앱 재시작 후 사라질 수 있어서 복사본을 저장한다. 이전 파일(oldPath)은 지운다.
 */
suspend fun importPickedImage(context: Context, uri: Uri, prefix: String, oldPath: String?): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "custom").apply { mkdirs() }
            val target = File(dir, "${prefix}_${System.currentTimeMillis()}.img")
            context.contentResolver.openInputStream(uri)!!.use { input ->
                target.outputStream().use { input.copyTo(it) }
            }
            oldPath?.let { File(it).delete() }
            target.absolutePath
        }.getOrNull()
    }

fun deleteCustomImage(path: String?) {
    path?.let { runCatching { File(it).delete() } }
}

/** 카드 뒷면: 사용자 사진이 있으면 사진, 없으면 테마 색 대각선 패턴. */
@Composable
fun CardBackFace(
    settings: LocalSettings,
    modifier: Modifier = Modifier.width(24.dp).height(36.dp),
) {
    val palette = boardPalette(settings.boardTheme)
    val image = rememberLocalImage(settings.cardBackImagePath, maxSidePx = 512)
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(palette.backTop, palette.backBottom)))
            .border(1.dp, palette.backAccent.copy(alpha = 0.7f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val step = 7.dp.toPx()
                        val stroke = 1.5.dp.toPx()
                        val color = palette.backAccent.copy(alpha = 0.22f)
                        var x = -size.height
                        while (x < size.width) {
                            drawLine(color, Offset(x, size.height), Offset(x + size.height, 0f), stroke)
                            x += step
                        }
                        val inset = 3.dp.toPx()
                        drawRoundRect(
                            color = palette.backAccent.copy(alpha = 0.55f),
                            topLeft = Offset(inset, inset),
                            size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                        )
                    },
            )
            Text("◆", color = palette.backAccent, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }
}
