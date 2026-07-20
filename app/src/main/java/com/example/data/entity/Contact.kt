package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val number: String,
    val remarks: String,
    val status: String = "PENDING", // PENDING, SENT
    val customPropertiesJson: String = "{}"
) {
    fun getCustomProperties(): Map<String, String> {
        return try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val adapter = moshi.adapter<Map<String, String>>(mapType)
            adapter.fromJson(customPropertiesJson) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
