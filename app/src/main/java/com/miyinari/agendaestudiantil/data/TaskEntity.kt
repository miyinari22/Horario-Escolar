package com.miyinari.agendaestudiantil.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val fecha: String,
    val nombre: String
)
