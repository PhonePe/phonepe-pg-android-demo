package com.example.phonepeoptions.adapter

data class SavedInstrumentsListItem(
    val type: String,
    val title: String,
    val subTitle: String?,
    val logoUrl: String,
    val accountId: String?,
    val isAvailable: Boolean,
    val priority: Int,
)