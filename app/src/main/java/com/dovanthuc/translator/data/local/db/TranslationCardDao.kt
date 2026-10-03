package com.dovanthuc.translator.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TranslationCardDao {

    @Query("SELECT * FROM translation_cards WHERE id != :excludeId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomCardExcluding(excludeId: Long): TranslationCardEntity?

    @Query("SELECT * FROM translation_cards ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomCard(): TranslationCardEntity?

    @Query("SELECT COUNT(*) FROM translation_cards")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<TranslationCardEntity>)

    @Query("DELETE FROM translation_cards")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceAll(cards: List<TranslationCardEntity>) {
        clearAll()
        insertAll(cards)
    }
}
