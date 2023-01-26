package com.example.talkspace.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.talkspace.model.SQLiteMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("select * from messages where receiverId = :receiverId or senderId = :receiverId order by timeStamp desc")
    fun getMessages(receiverId: String): Flow<List<SQLiteMessage>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: SQLiteMessage)
}