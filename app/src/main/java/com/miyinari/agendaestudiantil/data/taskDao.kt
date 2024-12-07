package com.miyinari.agendaestudiantil.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: kotlin.collections.List<com.miyinari.agendaestudiantil.data.TaskEntity>)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
