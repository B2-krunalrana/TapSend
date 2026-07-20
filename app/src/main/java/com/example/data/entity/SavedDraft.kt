package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_drafts")
data class SavedDraft(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val lastUsed: Long = System.currentTimeMillis()
)
