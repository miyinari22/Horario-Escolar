package com.miyinari.agendaestudiantil.clase

import android.os.Parcel
import android.os.Parcelable

data class Clase(
    val salon: String = "",
    val materia: String = "",
    val color: String = "",
    val horaInicio: String = "",
    val horaFin: String = "",
    val id: String = "",
    val dia: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(salon)
        parcel.writeString(materia)
        parcel.writeString(color)
        parcel.writeString(horaInicio)
        parcel.writeString(horaFin)
        parcel.writeString(id)
        parcel.writeString(dia)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Clase> {
        override fun createFromParcel(parcel: Parcel): Clase {
            return Clase(parcel)
        }

        override fun newArray(size: Int): Array<Clase?> {
            return arrayOfNulls(size)
        }
    }
}
