package com.example.talkspace.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.checkerframework.checker.nullness.qual.NonNull

data class FirebaseChat(
    val phoneNumber: String,
    var friendName: String,
    var friendAbout: String,
    var friendPhotoUrl: String,
    var lastChat: String,
    var lastTimeStamp: String,
    var remainingMessage: Int,
){
    fun toSQLObject(): SQLChat{
        return SQLChat(
            phoneNumber = phoneNumber,
            friendName = friendName,
            friendAbout = friendAbout,
            friendPhotoUri = friendPhotoUrl,
            lastChat = lastChat,
            lastTimeStamp = lastTimeStamp,
            remainingMessage = remainingMessage
        )
    }
}

@Entity(tableName = "chats")
data class SQLChat(
    @PrimaryKey val phoneNumber: String,
    @ColumnInfo(name = "friendName") var friendName: String,
    @ColumnInfo(name = "friendAbout") var friendAbout: String,
    @ColumnInfo(name = "friendPhotoUri") var friendPhotoUri: String,
    @ColumnInfo(name = "lastChat") var lastChat: String,
    @ColumnInfo(name = "lastTimeStamp") var lastTimeStamp: String,
    @ColumnInfo(name = "remainingMessages") var remainingMessage: Int
)