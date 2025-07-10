package com.example.languagecards.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCardDao {

    @Insert
    suspend fun insertWord(wordCardEntity: WordCardEntity)

    @Query("SELECT * FROM word_cards")
    fun getAllWords(): Flow<List<WordCardEntity>>

    @Query("""
            SELECT wc.* FROM word_cards AS wc
            JOIN word_cards_fts AS wcf ON wc.id = wcf.rowid
            WHERE wcf.word_cards_fts MATCH :query
        """)
    fun searchWords(query: String): Flow<List<WordCardEntity>>

    @Delete
    suspend fun deleteWord(wordCard: WordCardEntity)
}