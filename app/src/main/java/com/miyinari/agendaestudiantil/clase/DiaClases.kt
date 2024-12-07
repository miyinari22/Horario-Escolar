package com.miyinari.agendaestudiantil.clase

import android.os.Parcel
import android.os.Parcelable

data class DiaClases(
    val dia: String,
    val clases: MutableList<Clase>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        mutableListOf<Clase>().apply {
            parcel.readList(this, Clase::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dia)
        parcel.writeList(clases)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DiaClases> {
        override fun createFromParcel(parcel: Parcel): DiaClases {
            return DiaClases(parcel)
        }

        override fun newArray(size: Int): Array<DiaClases?> {
            return arrayOfNulls(size)
        }
    }
}
