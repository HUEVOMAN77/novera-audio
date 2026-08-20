package com.novera.audio

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class SmartRuleMode { FAVORITES, LONGEST, ARTIST }

data class SmartPlaylist(
    val id: String,
    val name: String,
    val mode: SmartRuleMode,
    val artistQuery: String = ""
)

data class Bookmark(
    val id: String,
    val trackId: String,
    val title: String,
    val positionMs: Long
)

data class LrcLine(val timeMs: Long, val text: String)

data class DeviceProfile(
    val key: String,
    val name: String,
    val bandLevels: Map<Int, Int> = emptyMap(),
    val bassBoost: Boolean = false,
    val loudness: Boolean = false,
    val spatial: Boolean = false
)

data class AdvancedState(
    val smartPlaylists: List<SmartPlaylist> = emptyList(),
    val bookmarks: Map<String, List<Bookmark>> = emptyMap(),
    val lrcByTrack: Map<String, List<LrcLine>> = emptyMap(),
    val deviceProfiles: List<DeviceProfile> = emptyList(),
    val loopA: Long? = null,
    val loopB: Long? = null,
    val loopEnabled: Boolean = false,
    val currentLyrics: List<LrcLine> = emptyList()
)

data class NoveraBackup(
    val playlists: List<Playlist>,
    val advanced: AdvancedState,
    val favorites: Set<String>,
    val themeOrdinal: Int
)

class AdvancedStore(context: Context) {
    private val prefs = context.getSharedPreferences("novera_advanced", Context.MODE_PRIVATE)

    fun load(): AdvancedState {
        val raw = prefs.getString("state", "{}") ?: "{}"
        return decode(runCatching { JSONObject(raw) }.getOrDefault(JSONObject()))
    }

    fun save(state: AdvancedState) {
        prefs.edit().putString("state", encode(state).toString()).apply()
    }

    fun encode(state: AdvancedState): JSONObject = JSONObject().apply {
        put("smartPlaylists", JSONArray().also { array -> state.smartPlaylists.forEach { item -> array.put(JSONObject().apply { put("id", item.id); put("name", item.name); put("mode", item.mode.name); put("artist", item.artistQuery) }) } })
        put("bookmarks", JSONArray().also { array -> state.bookmarks.forEach { (trackId, entries) -> entries.forEach { mark -> array.put(JSONObject().apply { put("trackId", trackId); put("id", mark.id); put("title", mark.title); put("positionMs", mark.positionMs) }) } } })
        put("lyrics", JSONArray().also { array -> state.lrcByTrack.forEach { (trackId, lines) -> lines.forEach { line -> array.put(JSONObject().apply { put("trackId", trackId); put("timeMs", line.timeMs); put("text", line.text) }) } } })
        put("profiles", JSONArray().also { array -> state.deviceProfiles.forEach { profile -> array.put(JSONObject().apply { put("key", profile.key); put("name", profile.name); put("bass", profile.bassBoost); put("loudness", profile.loudness); put("spatial", profile.spatial); put("bands", JSONObject().also { bands -> profile.bandLevels.forEach { (band, level) -> bands.put(band.toString(), level) } }) }) } })
        put("loopA", state.loopA ?: JSONObject.NULL)
        put("loopB", state.loopB ?: JSONObject.NULL)
        put("loopEnabled", state.loopEnabled)
    }

    fun decode(json: JSONObject): AdvancedState {
        val smart = buildList {
            val array = json.optJSONArray("smartPlaylists") ?: JSONArray()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                add(SmartPlaylist(item.optString("id", UUID.randomUUID().toString()), item.optString("name", "Novera Flow"), runCatching { SmartRuleMode.valueOf(item.optString("mode")) }.getOrDefault(SmartRuleMode.FAVORITES), item.optString("artist")))
            }
        }
        val bookmarks = mutableMapOf<String, MutableList<Bookmark>>()
        val bookmarkArray = json.optJSONArray("bookmarks") ?: JSONArray()
        for (i in 0 until bookmarkArray.length()) {
            val item = bookmarkArray.optJSONObject(i) ?: continue
            val trackId = item.optString("trackId")
            bookmarks.getOrPut(trackId) { mutableListOf() }.add(Bookmark(item.optString("id", UUID.randomUUID().toString()), trackId, item.optString("title", "Marcador"), item.optLong("positionMs")))
        }
        val lyrics = mutableMapOf<String, MutableList<LrcLine>>()
        val lyricArray = json.optJSONArray("lyrics") ?: JSONArray()
        for (i in 0 until lyricArray.length()) {
            val item = lyricArray.optJSONObject(i) ?: continue
            lyrics.getOrPut(item.optString("trackId")) { mutableListOf() }.add(LrcLine(item.optLong("timeMs"), item.optString("text")))
        }
        val profiles = buildList {
            val array = json.optJSONArray("profiles") ?: JSONArray()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val bandsObject = item.optJSONObject("bands") ?: JSONObject()
                val bands = mutableMapOf<Int, Int>()
                bandsObject.keys().forEach { key -> bands[key.toIntOrNull() ?: return@forEach] = bandsObject.optInt(key) }
                add(DeviceProfile(item.optString("key"), item.optString("name", "Salida de audio"), bands, item.optBoolean("bass"), item.optBoolean("loudness"), item.optBoolean("spatial")))
            }
        }
        return AdvancedState(smart, bookmarks, lyrics, profiles, json.optLongOrNull("loopA"), json.optLongOrNull("loopB"), json.optBoolean("loopEnabled"))
    }

    fun backup(playlists: List<Playlist>, advanced: AdvancedState, favorites: Set<String>, themeOrdinal: Int): JSONObject = JSONObject().apply {
        put("version", 1)
        put("playlists", JSONArray().also { array -> playlists.forEach { item -> array.put(JSONObject().apply { put("id", item.id); put("name", item.name); put("trackIds", JSONArray(item.trackIds)) }) } })
        put("advanced", encode(advanced))
        put("favorites", JSONArray(favorites.toList()))
        put("themeOrdinal", themeOrdinal)
    }

    fun restore(json: JSONObject): NoveraBackup {
        val playlistArray = json.optJSONArray("playlists") ?: JSONArray()
        val playlists = buildList {
            for (i in 0 until playlistArray.length()) {
                val item = playlistArray.optJSONObject(i) ?: continue
                val ids = item.optJSONArray("trackIds") ?: JSONArray()
                add(Playlist(item.optString("id", UUID.randomUUID().toString()), item.optString("name", "Playlist"), buildList { for (j in 0 until ids.length()) add(ids.optString(j)) }))
            }
        }
        val favoriteArray = json.optJSONArray("favorites") ?: JSONArray()
        val favorites = buildSet { for (i in 0 until favoriteArray.length()) add(favoriteArray.optString(i)) }
        return NoveraBackup(playlists, decode(json.optJSONObject("advanced") ?: JSONObject()), favorites, json.optInt("themeOrdinal", 0))
    }

    private fun JSONObject.optLongOrNull(key: String): Long? = if (isNull(key) || !has(key)) null else optLong(key)
}

object LrcParser {
    private val timestamp = Regex("\\[(\\d{1,3}):(\\d{2})(?:\\.(\\d{1,3}))?\\](.*)")

    fun parse(content: String): List<LrcLine> = content.lineSequence().flatMap { line ->
        val match = timestamp.matchEntire(line.trim()) ?: return@flatMap emptySequence()
        val minutes = match.groupValues[1].toLongOrNull() ?: 0L
        val seconds = match.groupValues[2].toLongOrNull() ?: 0L
        val fraction = match.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
        sequenceOf(LrcLine(minutes * 60_000L + seconds * 1_000L + fraction, match.groupValues[4].trim()))
    }.sortedBy { it.timeMs }.toList()
}
