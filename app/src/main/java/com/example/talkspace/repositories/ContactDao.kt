package com.example.talkspace.repositories

import androidx.room.*
import com.example.talkspace.model.SQLiteContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("select * from contacts where isAppUser = 0 order by contactName")
    fun getInContacts(): Flow<List<SQLiteContact>>

    @Query("select * from contacts where isAppUser = 1 order by contactName")
    fun getAppContacts(): Flow<List<SQLiteContact>>

    @Delete
    suspend fun delete(contact: SQLiteContact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: SQLiteContact)
}