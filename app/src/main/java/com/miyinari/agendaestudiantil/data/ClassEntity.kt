package com.miyinari.agendaestudiantil.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey val id: String,
    val horaInicio: String,
    val nombre: String
)
