package com.example.phonepeoptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.phonepeoptions.adapter.SavedInstrumentsListItem
import com.phonepe.intent.sdk.api.ppeInstruments.models.Instrument

class PaymentOptionsViewModel : ViewModel() {
    private val _isLinkButtonVisible = MutableLiveData(false)
    val isLinkButtonVisible: LiveData<Boolean> = _isLinkButtonVisible

    private val _savedInstrumentsList = MutableLiveData<List<SavedInstrumentsListItem>>()
    val savedInstrumentsList: LiveData<List<SavedInstrumentsListItem>> get() = _savedInstrumentsList

    private val _selectedInstrument = MutableLiveData<SavedInstrumentsListItem?>()
    val selectedInstrument: LiveData<SavedInstrumentsListItem?> = _selectedInstrument

    private val _isProgressBarVisible = MutableLiveData(false)
    val isProgressBarVisible: LiveData<Boolean> = _isProgressBarVisible

    fun getRandomString(length: Int): String {
        val allowedChars = ALPHA_NUMERIC_STRING
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

    fun setIsLinkButtonVisible(isVisible: Boolean) {
        _isLinkButtonVisible.postValue(isVisible)
    }

    fun updateSavedInstrumentsList(newList: List<SavedInstrumentsListItem>?) {
        _savedInstrumentsList.value = newList ?: emptyList()
    }

    fun setIsProgressBarVisible(isVisible: Boolean) {
        _isProgressBarVisible.postValue(isVisible)
    }

    fun setSelectedInstrument(selectedInstrumentListItem: SavedInstrumentsListItem?){
        _selectedInstrument.postValue(selectedInstrumentListItem)
    }

    companion object {
        private const val ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    }
}