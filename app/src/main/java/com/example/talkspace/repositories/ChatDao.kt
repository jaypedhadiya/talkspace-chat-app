package com.example.talkspace.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talkspace.model.SQLChat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("select * from chats order by lastTimeStamp desc")
    fun getChats(): Flow<List<SQLChat>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(chat: SQLChat)
}