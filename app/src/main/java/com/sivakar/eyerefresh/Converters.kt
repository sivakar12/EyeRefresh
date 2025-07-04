package com.sivakar.eyerefresh

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class Converters {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(AppState::class.java, AppStateTypeAdapter())
        .create()

    @TypeConverter
    fun fromAppState(appState: AppState): String {
        return gson.toJson(appState)
    }

    @TypeConverter
    fun toAppState(appStateString: String): AppState {
        return try {
            gson.fromJson(appStateString, AppState::class.java)
        } catch (e: Exception) {
            android.util.Log.e("Converters", "Failed to deserialize AppState: $appStateString", e)
            // Return a safe default state if deserialization fails
            AppState.RemindersPaused
        }
    }

    private class AppStateTypeAdapter : JsonSerializer<AppState>, JsonDeserializer<AppState> {
        override fun serialize(
            src: AppState?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            val json = JsonObject()
            when (src) {
                is AppState.RemindersPaused -> {
                    json.addProperty("type", "RemindersPaused")
                }
                is AppState.ReminderScheduled -> {
                    json.addProperty("type", "ReminderScheduled")
                    json.addProperty("timeInMillis", src.timeInMillis)
                }
                is AppState.ReminderSent -> {
                    json.addProperty("type", "ReminderSent")
                }
                is AppState.RefreshHappening -> {
                    json.addProperty("type", "RefreshHappening")
                }
                null -> {
                    json.addProperty("type", "null")
                }
            }
            return json
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): AppState {
            if (json == null || !json.isJsonObject) {
                throw IllegalArgumentException("Invalid JSON for AppState")
            }
            
            val jsonObject = json.asJsonObject
            
            // Try to get the type field first
            val type = jsonObject.get("type")?.asString
            
            return when (type) {
                "RemindersPaused" -> AppState.RemindersPaused
                "ReminderScheduled" -> {
                    val timeInMillis = jsonObject.get("timeInMillis")?.asLong
                        ?: throw IllegalArgumentException("timeInMillis is required for ReminderScheduled")
                    AppState.ReminderScheduled(timeInMillis)
                }
                "ReminderSent" -> AppState.ReminderSent
                "RefreshHappening" -> AppState.RefreshHappening
                null -> {
                    // Handle legacy data that might not have the type field
                    // This could be data serialized with the old Gson format
                    when {
                        jsonObject.has("timeInMillis") -> {
                            val timeInMillis = jsonObject.get("timeInMillis")?.asLong
                                ?: throw IllegalArgumentException("timeInMillis is required for ReminderScheduled")
                            AppState.ReminderScheduled(timeInMillis)
                        }
                        // For other cases, default to a safe state
                        // This handles cases where the JSON might be empty or have unexpected structure
                        else -> {
                            // Log the problematic JSON for debugging
                            android.util.Log.w("Converters", "Legacy AppState JSON without type field: $jsonObject")
                            AppState.RemindersPaused
                        }
                    }
                }
                else -> {
                    // Log unknown types for debugging
                    android.util.Log.w("Converters", "Unknown AppState type: $type, JSON: $jsonObject")
                    throw IllegalArgumentException("Unknown AppState type: $type")
                }
            }
        }
    }
}