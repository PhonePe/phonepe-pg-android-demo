package com.example.phonepeoptions.adapter

data class SavedInstrumentsListItem(
    val type: String,
    val title: String,
    val subTitle: String?,
    val logoUrl: String,
    val isAvailable: Boolean,
    val priority: Int,
    val networkLogoUrl: String?,
    val bankCode: String?,
    val id: String?,
    val metaInfo: Any?,
    val subType: String?
)