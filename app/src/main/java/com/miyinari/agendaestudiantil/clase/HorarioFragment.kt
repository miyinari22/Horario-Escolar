package com.miyinari.agendaestudiantil.clase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miyinari.agendaestudiantil.R

class HorarioFragment : Fragment() {

    private lateinit var rvHorario: RecyclerView
    private lateinit var fabAgregarClase: FloatingActionButton
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val listaDias = mutableListOf<DiaClases>()
    private lateinit var adapter: HorarioAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_horario, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        rvHorario = view.findViewById(R.id.rvHorario)
        rvHorario.layoutManager = LinearLayoutManager(requireContext())

        adapter = HorarioAdapter(listaDias) { clase ->
            val action = HorarioFragmentDirections.actionHorarioFragmentToClaseFragment(clase)
            findNavController().navigate(action)
        }

        rvHorario.adapter = adapter

        fabAgregarClase = view.findViewById(R.id.fabAgregarClase)
        fabAgregarClase.setOnClickListener {
            val nuevaClase = Clase(salon = "", materia = "", color = "", horaInicio = "", horaFin = "", dia = "")
            val action = HorarioFragmentDirections.actionHorarioFragmentToClaseFragment(nuevaClase, isEditMode = false)
            findNavController().navigate(action)
        }

        cargarClases()

        return view
    }

    private fun cargarClases() {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("users")
                .document(userEmail)
                .collection("clases")
                .get()
                .addOnSuccessListener { result ->
                    listaDias.clear()

                    val clases = mutableListOf<Clase>()
                    for (document in result) {
                        val clase = Clase(
                            id = document.getString("id") ?: "",
                            salon = document.getString("salon") ?: "",
                            materia = document.getString("materia") ?: "",
                            color = document.getString("color") ?: "",
                            horaInicio = document.getString("horaInicio") ?: "",
                            horaFin = document.getString("horaFin") ?: "",
                            dia = document.getString("dia") ?: ""
                        )
                        clases.add(clase)
                    }

                    val diasOrdenados = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
                    val clasesOrdenadas = clases.sortedWith(compareBy(
                        { diasOrdenados.indexOf(it.dia) },
                        { it.horaInicio }
                    ))

                    val diasClasesMap = clasesOrdenadas.groupBy { it.dia }
                    diasClasesMap.forEach { (dia, clasesList) ->
                        listaDias.add(DiaClases(dia, clasesList.toMutableList()))
                    }

                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al cargar las clases: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show()
        }
    }

}
