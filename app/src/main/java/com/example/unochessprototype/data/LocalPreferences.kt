package com.example.unochessprototype.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

private const val PREFS_NAME = "uno_chess_local"

data class LocalSettings(
    val bgmEnabled: Boolean = true,
    val effectsEnabled: Boolean = true,
    val bgmVolume: Float = 0.7f,
    val effectsVolume: Float = 0.8f,
    val moveHints: Boolean = true,
    val showCoordinates: Boolean = true,
    val boardTheme: Int = 0,
    /** 갤러리에서 고른 사진의 앱 내부 복사본 경로. 이 기기에서만 쓰인다. */
    val boardImagePath: String? = null,
    val cardBackImagePath: String? = null,
    /** 사용자가 이름을 붙여 저장한 '나만의 테마' 목록. */
    val themeSets: List<CustomThemeSet> = emptyList(),
)

/** 보드 프리셋 + 보드 사진 + 카드 뒷면 사진 한 벌. 사진은 앱 내부 복사본 경로. */
data class CustomThemeSet(
    val id: Long,
    val name: String,
    val baseTheme: Int,
    val boardImagePath: String?,
    val cardBackImagePath: String?,
)

/** 저장된 테마가 쓰는 사진 경로. 이 파일들은 지우면 안 된다. */
fun LocalSettings.savedSetImagePaths(): Set<String> =
    themeSets.flatMap { listOfNotNull(it.boardImagePath, it.cardBackImagePath) }.toSet()

class LocalPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadSettings(): LocalSettings = LocalSettings(
        bgmEnabled = prefs.getBoolean("bgmEnabled", true),
        effectsEnabled = prefs.getBoolean("effectsEnabled", true),
        bgmVolume = prefs.getFloat("bgmVolume", 0.7f),
        effectsVolume = prefs.getFloat("effectsVolume", 0.8f),
        moveHints = prefs.getBoolean("moveHints", true),
        showCoordinates = prefs.getBoolean("showCoordinates", true),
        boardTheme = prefs.getInt("boardTheme", 0),
        boardImagePath = prefs.getString("boardImagePath", null),
        cardBackImagePath = prefs.getString("cardBackImagePath", null),
        themeSets = decodeThemeSets(prefs.getString("themeSets", null)),
    )

    fun saveSettings(settings: LocalSettings) {
        prefs.edit()
            .putBoolean("bgmEnabled", settings.bgmEnabled)
            .putBoolean("effectsEnabled", settings.effectsEnabled)
            .putFloat("bgmVolume", settings.bgmVolume)
            .putFloat("effectsVolume", settings.effectsVolume)
            .putBoolean("moveHints", settings.moveHints)
            .putBoolean("showCoordinates", settings.showCoordinates)
            .putInt("boardTheme", settings.boardTheme)
            .putString("boardImagePath", settings.boardImagePath)
            .putString("cardBackImagePath", settings.cardBackImagePath)
            .putString("themeSets", encodeThemeSets(settings.themeSets))
            .apply()
    }

    private fun encodeThemeSets(sets: List<CustomThemeSet>): String = JSONArray().apply {
        sets.forEach { set ->
            put(
                JSONObject()
                    .put("id", set.id)
                    .put("name", set.name)
                    .put("baseTheme", set.baseTheme)
                    .put("board", set.boardImagePath ?: JSONObject.NULL)
                    .put("cardBack", set.cardBackImagePath ?: JSONObject.NULL)
            )
        }
    }.toString()

    // 저장값이 깨져 있으면 목록만 비우고 앱은 계속 동작한다.
    private fun decodeThemeSets(json: String?): List<CustomThemeSet> {
        if (json == null) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                CustomThemeSet(
                    id = o.getLong("id"),
                    name = o.getString("name"),
                    baseTheme = o.optInt("baseTheme", 0),
                    boardImagePath = if (o.isNull("board")) null else o.getString("board"),
                    cardBackImagePath = if (o.isNull("cardBack")) null else o.getString("cardBack"),
                )
            }
        }.getOrDefault(emptyList())
    }
}
