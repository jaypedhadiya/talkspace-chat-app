package com.example.talkspace.model

import androidx.lifecycle.LiveData


data class Section(
    val id : Int,
    val sectionTitle: String,
    val contacts : List<SQLiteContact>? = null
) {
}