package com.miyinari.agendaestudiantil.tarea

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.miyinari.agendaestudiantil.R

class TareasAdapter(
    private val tareasList: List<Tarea>,
    private val onItemClick: (Tarea) -> Unit
) : RecyclerView.Adapter<TareasAdapter.TareaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = tareasList[position]
        holder.bind(tarea)
    }

    override fun getItemCount(): Int {
        return tareasList.size
    }

    inner class TareaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTareaTextView: TextView = itemView.findViewById(R.id.tvNombreTarea)

        fun bind(tarea: Tarea) {
            nombreTareaTextView.text = "${tarea.nombre} - ${tarea.materias}\n Descripción: ${tarea.descripcion}"
            itemView.setOnClickListener {
                onItemClick(tarea)
            }
        }
    }
}
