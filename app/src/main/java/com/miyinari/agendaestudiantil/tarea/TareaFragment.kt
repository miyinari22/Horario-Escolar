package com.miyinari.agendaestudiantil.tarea

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
import com.google.firebase.firestore.ktx.toObject
import com.miyinari.agendaestudiantil.R

class TareaFragment : Fragment() {

    private lateinit var rvTareas: RecyclerView
    private lateinit var fabAgregarTarea: FloatingActionButton
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val tareasList = mutableListOf<Tarea>()
    private lateinit var adapter: TareasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tarea, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        rvTareas = view.findViewById(R.id.rvTareas)
        rvTareas.layoutManager = LinearLayoutManager(requireContext())

        adapter = TareasAdapter(tareasList) { tarea ->
            val action = TareaFragmentDirections.actionTareaFragmentToEditTareaFragment(tarea)
            findNavController().navigate(action)
        }
        rvTareas.adapter = adapter

        fabAgregarTarea = view.findViewById(R.id.fabAgregarTarea)
        fabAgregarTarea.setOnClickListener {
            val nuevaTarea = Tarea(id = "", nombre = "", descripcion = "", fecha = "", hora = "")
            val action = TareaFragmentDirections.actionTareaFragmentToEditTareaFragment(nuevaTarea, isEditMode = false)
            findNavController().navigate(action)
        }

        cargarTareas()

        return view
    }

    private fun cargarTareas() {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("users")
                .document(userEmail)
                .collection("tareas")
                .get()
                .addOnSuccessListener { result ->
                    tareasList.clear()
                    for (document in result) {
                        val tarea = document.toObject<Tarea>().copy(id = document.id)
                        tareasList.add(tarea)
                    }
                    adapter.notifyDataSetChanged()

                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al cargar las tareas: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show()
        }
    }
}
