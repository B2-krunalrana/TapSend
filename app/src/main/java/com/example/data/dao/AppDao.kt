package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.Campaign
import com.example.data.entity.Contact
import com.example.data.entity.MessageTemplate
import com.example.data.entity.SavedDraft
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Contacts operations
    @Query("SELECT * FROM contacts ORDER BY id ASC")
    fun getAllContactsFlow(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Query("UPDATE contacts SET status = :status WHERE id = :contactId")
    suspend fun updateContactStatus(contactId: Int, status: String)

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    // Message Template operations
    @Query("SELECT * FROM message_template WHERE id = 1 LIMIT 1")
    fun getTemplateFlow(): Flow<MessageTemplate?>

    @Query("SELECT * FROM message_template WHERE id = 1 LIMIT 1")
    suspend fun getTemplateDirect(): MessageTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: MessageTemplate)

    // Campaign operations
    @Query("SELECT * FROM campaigns ORDER BY timestamp DESC")
    fun getAllCampaignsFlow(): Flow<List<Campaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)

    @Query("DELETE FROM campaigns WHERE id = :campaignId")
    suspend fun deleteCampaign(campaignId: Int)

    @Query("DELETE FROM campaigns")
    suspend fun deleteAllCampaigns()

    // Saved Drafts operations
    @Query("SELECT * FROM saved_drafts ORDER BY lastUsed DESC")
    fun getAllSavedDraftsFlow(): Flow<List<SavedDraft>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedDraft(savedDraft: SavedDraft)

    @Query("DELETE FROM saved_drafts WHERE id = :savedDraftId")
    suspend fun deleteSavedDraft(savedDraftId: Int)
}
