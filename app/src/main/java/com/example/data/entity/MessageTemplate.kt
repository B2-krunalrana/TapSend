package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_template")
data class MessageTemplate(
    @PrimaryKey val id: Int = 1, // Single row constraint
    val templateText: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
