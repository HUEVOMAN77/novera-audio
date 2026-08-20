package com.novera.audio

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Playlist(
    val id: String,
    val name: String,
    val trackIds: List<String> = emptyList()
)

class PlaylistStore(context: Context) {
    private val prefs = context.getSharedPreferences("novera_playlists", Context.MODE_PRIVATE)

    fun load(): List<Playlist> {
        val raw = prefs.getString("items", "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val tracks = item.optJSONArray("trackIds") ?: JSONArray()
                    add(
                        Playlist(
                            id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                            name = item.optString("name").ifBlank { "Playlist sin nombre" },
                            trackIds = buildList { for (trackIndex in 0 until tracks.length()) add(tracks.optString(trackIndex)) }
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(playlists: List<Playlist>) {
        val array = JSONArray()
        playlists.forEach { playlist ->
            array.put(
                JSONObject().apply {
                    put("id", playlist.id)
                    put("name", playlist.name)
                    put("trackIds", JSONArray(playlist.trackIds))
                }
            )
        }
        prefs.edit().putString("items", array.toString()).apply()
    }
}
