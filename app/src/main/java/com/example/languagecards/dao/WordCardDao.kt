package com.example.languagecards.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordCardDao {

    @Insert
    suspend fun insertWord(wordCardEntity: WordCardEntity)

    @Update
    suspend fun updateWord(wordCardEntity: WordCardEntity)

    @Query("SELECT * FROM word_cards WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Int): WordCardEntity?

    @Query("SELECT * FROM word_cards WHERE language = :language ORDER BY foreign_word ASC")
    fun getAllWords(language: Int): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards ORDER BY foreign_word ASC")
    fun getAllWordsAllLanguages(): Flow<List<WordCardEntity>>

    @Query("""
            SELECT wc.* FROM word_cards AS wc
            JOIN word_cards_fts AS wcf ON wc.id = wcf.rowid
            WHERE wcf.word_cards_fts MATCH :query AND wc.language = :language
        """)
    fun searchWords(query: String, language: Int): Flow<List<WordCardEntity>>

    @Query("SELECT * FROM word_cards WHERE language = :language ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWords(limit: Int, language: Int): List<WordCardEntity>

    @Delete
    suspend fun deleteWord(wordCard: WordCardEntity)
}