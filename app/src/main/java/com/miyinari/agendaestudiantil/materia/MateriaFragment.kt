package com.miyinari.agendaestudiantil.materia

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

class MateriaFragment : Fragment() {

    private lateinit var rvMaterias: RecyclerView
    private lateinit var fabAgregarMateria: FloatingActionButton
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val materiasList = mutableListOf<Materia>()
    private lateinit var adapter: MateriasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_materia, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        rvMaterias = view.findViewById(R.id.rvMaterias)
        fabAgregarMateria = view.findViewById(R.id.fabAgregarMateria)

        adapter = MateriasAdapter(materiasList) { materia ->
            val action = MateriaFragmentDirections.actionMateriaFragmentToEditMateriaFragment(materia)
            findNavController().navigate(action)
        }
        rvMaterias.layoutManager = LinearLayoutManager(requireContext())
        rvMaterias.adapter = adapter

        cargarMaterias()

        fabAgregarMateria.setOnClickListener {
            val nuevaMateria = Materia(id = "", nombre = "", profesor = "")
            val action = MateriaFragmentDirections.actionMateriaFragmentToEditMateriaFragment(nuevaMateria, isEditMode = false)
            findNavController().navigate(action)
        }

        return view
    }

    private fun cargarMaterias() {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            firestore.collection("users")
                .document(userEmail)
                .collection("materias")
                .get()
                .addOnSuccessListener { result ->
                    materiasList.clear()
                    for (document in result) {
                        val materia = document.toObject<Materia>().copy(id = document.id)
                        materiasList.add(materia)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al cargar las materias: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
