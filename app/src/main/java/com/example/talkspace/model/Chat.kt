package com.example.talkspace.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.checkerframework.checker.nullness.qual.NonNull

data class FirebaseChat(
    val phoneNumber: String,
    val friendName: String,
    val friendAbout: String,
    val friendPhotoUrl: String,
    val lastChat: String,
    val lastTimeStamp: String,
    val remainingMessage: Int,
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
    @ColumnInfo(name = "friendName") val friendName: String,
    @ColumnInfo(name = "friendAbout") val friendAbout: String,
    @ColumnInfo(name = "friendPhotoUri") val friendPhotoUri: String,
    @ColumnInfo(name = "lastChat") val lastChat: String,
    @ColumnInfo(name = "lastTimeStamp") val lastTimeStamp: String,
    @ColumnInfo(name = "remainingMessages") val remainingMessage: Int
)