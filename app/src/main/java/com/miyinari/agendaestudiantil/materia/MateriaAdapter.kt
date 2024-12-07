package com.miyinari.agendaestudiantil.materia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.miyinari.agendaestudiantil.R

class MateriasAdapter(
    private val materiasList: List<Materia>,
    private val onItemClick: (Materia) -> Unit
) : RecyclerView.Adapter<MateriasAdapter.MateriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_materia, parent, false)
        return MateriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        val materia = materiasList[position]
        holder.bind(materia)
    }

    override fun getItemCount(): Int {
        return materiasList.size
    }

    inner class MateriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreMateriaTextView: TextView = itemView.findViewById(R.id.tvNombreMateria)

        fun bind(materia: Materia) {
            nombreMateriaTextView.text = "${materia.nombre} - ${materia.profesor}"

            itemView.setOnClickListener {
                onItemClick(materia)
            }
        }
    }
}
