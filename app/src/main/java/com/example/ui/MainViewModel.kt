package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Campaign
import com.example.data.entity.Contact
import com.example.data.entity.MessageTemplate
import com.example.data.entity.SavedDraft
import com.example.data.repository.ContactRepository
import com.example.utils.CsvParser
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.InputStream

enum class Step {
    UPLOAD,
    COMPOSE,
    SEND,
    HISTORY
}

class MainViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _currentStep = MutableStateFlow(Step.UPLOAD)
    val currentStep: StateFlow<Step> = _currentStep.asStateFlow()

    val allContacts: StateFlow<List<Contact>> = repository.allContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCampaigns: StateFlow<List<Campaign>> = repository.allCampaigns
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSavedDrafts: StateFlow<List<SavedDraft>> = repository.allSavedDrafts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val availableVariables: StateFlow<List<String>> = allContacts.map { contacts ->
        if (contacts.isEmpty()) {
            listOf("name", "number", "remarks")
        } else {
            val firstContact = contacts.first()
            val customKeys = getContactCustomProperties(firstContact).keys.toList()
            val defaultKeys = listOf("name", "number", "remarks")
            (defaultKeys + customKeys).distinct()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("name", "number", "remarks"))

    private val _templateText = MutableStateFlow("Hi {{name}}, your order with remarks: \"{{remarks}}\" is ready for pickup at {{number}}!")
    val templateText: StateFlow<String> = _templateText.asStateFlow()

    // Temporary list for step 1 preview
    private val _previewContacts = MutableStateFlow<List<Contact>>(emptyList())
    val previewContacts: StateFlow<List<Contact>> = _previewContacts.asStateFlow()

    private val _csvFileName = MutableStateFlow<String?>(null)
    val csvFileName: StateFlow<String?> = _csvFileName.asStateFlow()

    private val _csvError = MutableStateFlow<String?>(null)
    val csvError: StateFlow<String?> = _csvError.asStateFlow()

    // Tracking card stack state locally
    private val _stackContacts = MutableStateFlow<List<Contact>>(emptyList())
    val stackContacts: StateFlow<List<Contact>> = _stackContacts.asStateFlow()

    // ID of the contact that is currently being sent (for resume auto-mark)
    private var pendingSentContactId: Int? = null

    init {
        // Load message template on startup
        viewModelScope.launch {
            val savedTemplate = repository.getTemplateDirect()
            if (savedTemplate != null) {
                _templateText.value = savedTemplate.templateText
            }
            
            // Check if we can resume last session
            val contacts = repository.allContacts.first()
            if (contacts.isNotEmpty()) {
                val hasPending = contacts.any { it.status == "PENDING" }
                if (hasPending) {
                    _currentStep.value = Step.SEND
                } else {
                    // All sent, let's keep them in Step 3 showing the "All Done" screen
                    _currentStep.value = Step.SEND
                }
            }
        }

        // Sync stack contacts with PENDING ones from database while preserving skip/stack order
        viewModelScope.launch {
            allContacts.collect { contacts ->
                val pending = contacts.filter { it.status == "PENDING" }
                val currentStack = _stackContacts.value
                if (currentStack.isEmpty()) {
                    _stackContacts.value = pending
                } else {
                    val pendingIds = pending.map { it.id }.toSet()
                    val filteredStack = currentStack.filter { it.id in pendingIds }
                    val existingIds = filteredStack.map { it.id }.toSet()
                    val newPending = pending.filter { it.id !in existingIds }
                    _stackContacts.value = filteredStack + newPending
                }
            }
        }
    }

    fun handleCsvUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _csvError.value = null
            _previewContacts.value = emptyList()
            try {
                val contentResolver = context.contentResolver
                // Get file name
                val cursor = contentResolver.query(uri, null, null, null, null)
                var name = "uploaded_list.csv"
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = it.getString(nameIndex)
                        }
                    }
                }
                _csvFileName.value = name

                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    parseAndSetPreview(inputStream)
                } else {
                    _csvError.value = "Failed to open selected CSV file."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _csvError.value = "Error reading file: ${e.localizedMessage}"
            }
        }
    }

    private fun parseAndSetPreview(inputStream: InputStream) {
        val parsed = CsvParser.parseCsv(inputStream)
        if (parsed.isEmpty()) {
            _csvError.value = "The CSV file is empty or formatted incorrectly."
            return
        }

        // Search for headers case-insensitively
        val firstRowKeys = parsed.first().keys
        val nameKey = firstRowKeys.find { it.contains("name") }
        val numberKey = firstRowKeys.find { it.contains("number") || it.contains("phone") || it.contains("mobile") }
        val remarksKey = firstRowKeys.find { it.contains("remark") || it.contains("comment") || it.contains("note") }
        val countryCodeKey = firstRowKeys.find { it.contains("country") || it.contains("code") || it.contains("cc") }

        if (numberKey == null) {
            _csvError.value = "We couldn't find a 'Number' column in your CSV. Ensure a column header has 'number', 'phone', or 'mobile'."
            return
        }

        val contactsList = parsed.map { row ->
            val rawNumber = row[numberKey] ?: ""
            val rawCc = countryCodeKey?.let { row[it] } ?: ""
            val cleanCc = rawCc.filter { it.isDigit() || it == '+' }
            val cleanNum = rawNumber.filter { it.isDigit() || it == '+' }
            
            val finalNumber = if (cleanCc.isNotEmpty() && !cleanNum.startsWith("+") && cleanNum.length < 11) {
                val ccPrefix = if (cleanCc.startsWith("+")) cleanCc else "+$cleanCc"
                "$ccPrefix$cleanNum"
            } else {
                cleanNum
            }

            // Exclude main columns from customProperties to prevent duplicate rendering, keep only unique custom keys
            val customMap = row.filterKeys { k -> 
                k != nameKey && k != numberKey && k != remarksKey && k != countryCodeKey 
            }
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val jsonAdapter = moshi.adapter<Map<String, String>>(mapType)
            val customPropertiesJson = jsonAdapter.toJson(customMap)

            Contact(
                name = row[nameKey] ?: "Contact",
                number = finalNumber,
                remarks = row[remarksKey] ?: "",
                customPropertiesJson = customPropertiesJson
            )
        }.filter { it.number.isNotBlank() }

        if (contactsList.isEmpty()) {
            _csvError.value = "No valid contacts with phone numbers were found in this CSV."
            return
        }

        _previewContacts.value = contactsList
    }

    fun confirmImport() {
        val contactsToSave = _previewContacts.value
        if (contactsToSave.isEmpty()) return

        viewModelScope.launch {
            repository.deleteAllContacts()
            repository.insertContacts(contactsToSave)
            
            // Save to Campaign History
            try {
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val listType = Types.newParameterizedType(List::class.java, Contact::class.java)
                val jsonAdapter = moshi.adapter<List<Contact>>(listType)
                val contactsJson = jsonAdapter.toJson(contactsToSave)
                
                repository.insertCampaign(
                    Campaign(
                        name = _csvFileName.value ?: "imported_list.csv",
                        contactsCount = contactsToSave.size,
                        contactsJson = contactsJson
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _previewContacts.value = emptyList()
            _csvFileName.value = null
            _csvError.value = null
            _currentStep.value = Step.COMPOSE
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val jsonAdapter = moshi.adapter<Map<String, String>>(mapType)

            val demoContacts = listOf(
                Contact(
                    name = "Krunal Rana",
                    number = "+91 9827042001",
                    remarks = "Express Delivery",
                    customPropertiesJson = jsonAdapter.toJson(mapOf("amount" to "$150", "date" to "July 20"))
                ),
                Contact(
                    name = "Sarah Miller",
                    number = "+44 7700 900077",
                    remarks = "Fragile Package",
                    customPropertiesJson = jsonAdapter.toJson(mapOf("amount" to "$45", "date" to "July 21"))
                ),
                Contact(
                    name = "Robert King",
                    number = "+91 98765 43210",
                    remarks = "COD Pending",
                    customPropertiesJson = jsonAdapter.toJson(mapOf("amount" to "$99", "date" to "July 22"))
                )
            )
            _previewContacts.value = demoContacts
            _csvFileName.value = "sample_contacts.csv"
            _csvError.value = null
        }
    }

    fun removeSelectedFile() {
        _previewContacts.value = emptyList()
        _csvFileName.value = null
        _csvError.value = null
    }

    fun saveTemplate(text: String) {
        _templateText.value = text
        viewModelScope.launch {
            repository.saveTemplate(text)
        }
    }

    fun insertVariable(variable: String) {
        val current = _templateText.value
        _templateText.value = "$current{{$variable}}"
        viewModelScope.launch {
            repository.saveTemplate(_templateText.value)
        }
    }

    fun generateLinks() {
        viewModelScope.launch {
            repository.saveTemplate(_templateText.value)
            
            // Auto save Draft Message to History if it doesn't already exist as the latest
            val textToSave = _templateText.value
            if (textToSave.isNotBlank()) {
                val currentDrafts = repository.allSavedDrafts.first()
                if (currentDrafts.none { it.text.trim() == textToSave.trim() }) {
                    repository.insertSavedDraft(SavedDraft(text = textToSave))
                }
            }

            val contacts = repository.allContacts.first()
            _stackContacts.value = contacts.filter { it.status == "PENDING" }
            _currentStep.value = Step.SEND
        }
    }

    fun saveDraftToHistory(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertSavedDraft(SavedDraft(text = text))
        }
    }

    fun applySavedDraft(text: String) {
        _templateText.value = text
        viewModelScope.launch {
            repository.saveTemplate(text)
        }
    }

    fun deleteSavedDraft(draftId: Int) {
        viewModelScope.launch {
            repository.deleteSavedDraft(draftId)
        }
    }

    fun restoreCampaign(campaign: Campaign) {
        viewModelScope.launch {
            try {
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val listType = Types.newParameterizedType(List::class.java, Contact::class.java)
                val jsonAdapter = moshi.adapter<List<Contact>>(listType)
                val contacts = jsonAdapter.fromJson(campaign.contactsJson) ?: emptyList()
                
                if (contacts.isNotEmpty()) {
                    repository.deleteAllContacts()
                    val resetContacts = contacts.map { it.copy(id = 0, status = "PENDING") }
                    repository.insertContacts(resetContacts)
                    _previewContacts.value = emptyList()
                    _csvFileName.value = campaign.name
                    _currentStep.value = Step.COMPOSE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteCampaign(campaignId: Int) {
        viewModelScope.launch {
            repository.deleteCampaign(campaignId)
        }
    }

    fun setStep(step: Step) {
        _currentStep.value = step
    }

    fun setPendingSentContact(id: Int) {
        pendingSentContactId = id
    }

    fun onActivityResume() {
        val pendingId = pendingSentContactId
        if (pendingId != null) {
            markAsSent(pendingId)
            pendingSentContactId = null
        }
    }

    fun markAsSent(contactId: Int) {
        viewModelScope.launch {
            repository.updateContactStatus(contactId, "SENT")
            _stackContacts.value = _stackContacts.value.filter { it.id != contactId }
        }
    }

    fun moveToBottom(contactId: Int) {
        val currentList = _stackContacts.value
        val contactToMove = currentList.find { it.id == contactId }
        if (contactToMove != null) {
            val updated = currentList.filter { it.id != contactId } + contactToMove
            _stackContacts.value = updated
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            repository.deleteAllContacts()
            _previewContacts.value = emptyList()
            _csvFileName.value = null
            _csvError.value = null
            _stackContacts.value = emptyList()
            _currentStep.value = Step.UPLOAD
        }
    }

    private fun getContactCustomProperties(contact: Contact): Map<String, String> {
        val json = contact.customPropertiesJson
        if (json.isEmpty()) return emptyMap()
        return try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
            val adapter = moshi.adapter<Map<String, String>>(mapType)
            adapter.fromJson(json) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

class MainViewModelFactory(private val repository: ContactRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
