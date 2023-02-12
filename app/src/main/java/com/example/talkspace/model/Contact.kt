package com.example.talkspace.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

data class FirebaseContact(
    val contactPhoneNumber: String,
    var contactName: String,
    var contactAbout: String,
    var contactPhotoUrl: String,
    var isAppUser: Boolean
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
    @ColumnInfo(name= "contactName") var contactName: String,
    @ColumnInfo(name = "contactAbout") var contactAbout: String,
    @ColumnInfo("contactPhotoUrl") var contactPhotoUrl: String,
    @ColumnInfo(name = "isAppUser") var isAppUser: Boolean
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
