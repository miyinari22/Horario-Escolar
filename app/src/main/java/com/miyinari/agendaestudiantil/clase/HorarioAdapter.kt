package com.miyinari.agendaestudiantil.clase

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.miyinari.agendaestudiantil.R

class HorarioAdapter(
    private val diasClases: List<DiaClases>,
    private val onClaseClick: (Clase) -> Unit
) : RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder>() {

    inner class HorarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDia: TextView = view.findViewById(R.id.tvDia)
        val tvNombreMateria: TextView = view.findViewById(R.id.tvNombreMateria)
        val cardViewClase: View = view.findViewById(R.id.cardViewClase)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clase, parent, false)
        return HorarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        var currentPos = position
        var diaClase: DiaClases? = null
        var clase: Clase? = null

        for (dia in diasClases) {
            if (currentPos < dia.clases.size) {
                diaClase = dia
                clase = dia.clases[currentPos]
                break
            }
            currentPos -= dia.clases.size
        }

        diaClase?.let {
            holder.tvDia.text = it.dia
            holder.tvNombreMateria.text = "${clase?.materia} - ${clase?.salon} [${clase?.horaInicio} - ${clase?.horaFin}]"

            try {
                holder.tvNombreMateria.setBackgroundColor(Color.parseColor(clase?.color ?: "#FFFFFF"))
            } catch (e: IllegalArgumentException) {
                holder.tvNombreMateria.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            holder.tvDia.visibility = if (currentPos == 0) View.VISIBLE else View.GONE

            holder.cardViewClase.setOnClickListener {
                clase?.let { onClaseClick(it) }
            }
        }
    }

    override fun getItemCount(): Int {
        return diasClases.sumOf { it.clases.size }
    }
}
