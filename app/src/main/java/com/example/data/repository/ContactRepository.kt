package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.entity.Campaign
import com.example.data.entity.Contact
import com.example.data.entity.MessageTemplate
import com.example.data.entity.SavedDraft
import kotlinx.coroutines.flow.Flow

class ContactRepository(private val appDao: AppDao) {

    val allContacts: Flow<List<Contact>> = appDao.getAllContactsFlow()
    val template: Flow<MessageTemplate?> = appDao.getTemplateFlow()
    val allCampaigns: Flow<List<Campaign>> = appDao.getAllCampaignsFlow()
    val allSavedDrafts: Flow<List<SavedDraft>> = appDao.getAllSavedDraftsFlow()

    suspend fun getTemplateDirect(): MessageTemplate? {
        return appDao.getTemplateDirect()
    }

    suspend fun insertContacts(contacts: List<Contact>) {
        appDao.insertContacts(contacts)
    }

    suspend fun updateContactStatus(contactId: Int, status: String) {
        appDao.updateContactStatus(contactId, status)
    }

    suspend fun deleteAllContacts() {
        appDao.deleteAllContacts()
    }

    suspend fun saveTemplate(templateText: String) {
        appDao.insertTemplate(
            MessageTemplate(
                id = 1,
                templateText = templateText,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }

    suspend fun insertCampaign(campaign: Campaign) {
        appDao.insertCampaign(campaign)
    }

    suspend fun deleteCampaign(campaignId: Int) {
        appDao.deleteCampaign(campaignId)
    }

    suspend fun insertSavedDraft(savedDraft: SavedDraft) {
        appDao.insertSavedDraft(savedDraft)
    }

    suspend fun deleteSavedDraft(savedDraftId: Int) {
        appDao.deleteSavedDraft(savedDraftId)
    }
}
