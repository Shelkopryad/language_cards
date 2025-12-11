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
    suspend fun insertWord(wordCardEntity: WordCardEntity): Long

    @Insert
    suspend fun insertTranslation(translationEntity: TranslationEntity)

    @Update
    suspend fun updateWord(wordCardEntity: WordCardEntity)

    @Delete
    suspend fun deleteWord(wordCard: WordCardEntity)

    @Query("SELECT * FROM word_cards WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Int): WordCardEntity?

    @androidx.room.Transaction
    @Query("SELECT * FROM word_cards WHERE id = :id LIMIT 1")
    suspend fun getWordWithTranslationsById(id: Int): WordWithTranslations?

    @androidx.room.Transaction
    @Query("SELECT * FROM word_cards WHERE language = :language ORDER BY full_word ASC")
    fun getAllWordsWithTranslations(language: Int): Flow<List<WordWithTranslations>>

    @androidx.room.Transaction
    @Query("SELECT * FROM word_cards ORDER BY full_word ASC")
    fun getAllWordsWithTranslationsAllLanguages(): Flow<List<WordWithTranslations>>

    @Query("SELECT * FROM translations WHERE word_card_id = :wordId")
    suspend fun getTranslationsForWord(wordId: Int): List<TranslationEntity>

    @Query("DELETE FROM translations WHERE word_card_id = :wordId")
    suspend fun deleteTranslationsForWord(wordId: Int)

    @androidx.room.Transaction
    @Query("""
        SELECT DISTINCT wc.* FROM word_cards AS wc
        LEFT JOIN translations AS t ON wc.id = t.word_card_id
        WHERE (wc.full_word LIKE '%' || :query || '%' OR t.translation LIKE '%' || :query || '%')
        AND wc.language = :language
        ORDER BY wc.full_word ASC
    """)
    fun searchWordsWithTranslations(query: String, language: Int): Flow<List<WordWithTranslations>>
}