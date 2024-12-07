package com.miyinari.agendaestudiantil.tarea

import android.os.Parcel
import android.os.Parcelable

data class Tarea(
    val nombre: String = "",
    val descripcion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val id: String = "",
    val materias: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(descripcion)
        parcel.writeString(fecha)
        parcel.writeString(hora)
        parcel.writeString(id)
        parcel.writeString(materias)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Tarea> {
        override fun createFromParcel(parcel: Parcel): Tarea {
            return Tarea(parcel)
        }

        override fun newArray(size: Int): Array<Tarea?> {
            return arrayOfNulls(size)
        }
    }
}
