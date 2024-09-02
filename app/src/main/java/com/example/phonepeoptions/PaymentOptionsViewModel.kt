package com.example.phonepeoptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phonepeoptions.adapter.SavedInstrumentsListItem
import com.phonepe.intent.sdk.api.ppeInstruments.models.Instrument

class PaymentOptionsViewModel : ViewModel() {
    private val _showLinkButton = MutableLiveData(false)
    val showLinkButton: LiveData<Boolean> = _showLinkButton

    private val _savedInstrumentsList = MutableLiveData<List<SavedInstrumentsListItem>>()
    val savedInstrumentsList: LiveData<List<SavedInstrumentsListItem>> get() = _savedInstrumentsList

    private val _selectedInstrument = MutableLiveData<SavedInstrumentsListItem?>()
    val selectedInstrument: LiveData<SavedInstrumentsListItem?> = _selectedInstrument

    private val _showProgressBar = MutableLiveData(false)
    val showProgressBar: LiveData<Boolean> = _showProgressBar

    fun getRandomString(length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    fun convertPaymentOptionsListToSavedInstrumentsList(instruments: List<Instrument>?): List<SavedInstrumentsListItem>? {
        return instruments?.map {
            SavedInstrumentsListItem(
                it.type,
                it.title,
                it.subTitle,
                it.logoUrl,
                it.accountId,
                it.isAvailable,
                it.priority
            )
        }
    }

    fun setShowLinkButton(visibility: Boolean) {
        _showLinkButton.postValue(visibility)
    }

    fun updateSavedInstrumentsList(newList: List<SavedInstrumentsListItem>?) {
        _savedInstrumentsList.value = newList ?: emptyList()
    }


    fun setShowProgressBar(visibility: Boolean) {
        _showProgressBar.postValue(visibility)
    }

    fun setSelectedInstrument(selectedInstrumentListItem: SavedInstrumentsListItem?){
        _selectedInstrument.postValue(selectedInstrumentListItem)
    }
}