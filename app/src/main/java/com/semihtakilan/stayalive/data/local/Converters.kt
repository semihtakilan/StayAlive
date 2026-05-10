package com.semihtakilan.stayalive.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * JSON serialization for [Measurement.tags] — keys only, never translated UI strings.
 */
class Converters {

    private val gson = Gson()

    @TypeConverter
    fun tagsToJson(tags: List<String>?): String =
        gson.toJson(tags ?: emptyList<String>())

    @TypeConverter
    fun jsonToTags(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
