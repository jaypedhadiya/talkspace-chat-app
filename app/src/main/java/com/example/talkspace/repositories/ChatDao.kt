package com.example.talkspace.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talkspace.model.SQLChat
import com.example.talkspace.model.SQLiteContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("select * from chats order by lastTimeStamp desc")
    fun getChats(): Flow<List<SQLChat>>

    @Query("select * from contacts where contactPhoneNumber = :phoneNumber")
    fun checkContact(phoneNumber: String): SQLiteContact

    @Query("delete from chats where phoneNumber = :phoneNumber")
    fun delete(phoneNumber: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: SQLChat)
}