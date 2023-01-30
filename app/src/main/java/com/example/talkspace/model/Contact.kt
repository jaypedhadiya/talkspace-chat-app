package com.example.talkspace.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class FirebaseContact(
    val contactPhoneNumber: String,
    val contactName: String,
    val contactAbout: String,
    val contactPhotoUrl: String,
    val isAppUser: Boolean
){
    fun toSQLiteObject():SQLiteContact {
        return SQLiteContact(
            contactPhoneNumber = contactPhoneNumber,
            contactName = contactName,
            contactAbout = contactAbout,
            contactPhotoUrl = contactPhotoUrl,
            isAppUser = isAppUser
        )
    }
}

@Entity(tableName ="contacts")
data class SQLiteContact(
    @PrimaryKey val contactPhoneNumber: String,
    @ColumnInfo(name= "contactName") val contactName: String,
    @ColumnInfo(name = "contactAbout") val contactAbout: String,
    @ColumnInfo("contactPhotoUrl") val contactPhotoUrl: String,
    @ColumnInfo(name = "isAppUser") val isAppUser: Boolean
){
    fun toFirebaseContact():FirebaseContact{
        return FirebaseContact(
            contactPhoneNumber = contactPhoneNumber,
            contactName = contactName,
            contactAbout = contactAbout,
            contactPhotoUrl = contactPhotoUrl,
            isAppUser = isAppUser
        )
    }
}
