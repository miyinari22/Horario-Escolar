package com.miyinari.agendaestudiantil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(cl: kotlin.collections.List<com.miyinari.agendaestudiantil.data.ClassEntity>)

    @Query("SELECT * FROM classes")
    suspend fun getAllClasses(): List<ClassEntity>

    @Query("DELETE FROM classes")
    suspend fun deleteAllClasses()
}
