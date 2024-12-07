package com.miyinari.agendaestudiantil.materia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.miyinari.agendaestudiantil.R

class EditMateriaFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var inputNombreMateria: TextInputEditText
    private lateinit var inputProfesorMateria: TextInputEditText
    private lateinit var btnGuardarMateria: MaterialButton
    private lateinit var btnEliminarMateria: MaterialButton
    private var materia: Materia? = null

    private val args: EditMateriaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_materia, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        inputNombreMateria = view.findViewById(R.id.etMateria)
        inputProfesorMateria = view.findViewById(R.id.etProfesor)
        btnGuardarMateria = view.findViewById(R.id.btnGuardarMateria)
        btnEliminarMateria = view.findViewById(R.id.btnEliminarMateria)

        materia = arguments?.getParcelable("materia")

        if (args.isEditMode) {
            btnEliminarMateria.visibility = View.VISIBLE
        } else {
            btnEliminarMateria.visibility = View.GONE
        }

        materia?.let {
            inputNombreMateria.setText(it.nombre)
            inputProfesorMateria.setText(it.profesor)
        }

        btnGuardarMateria.setOnClickListener {
            verificarYGuardarMateria()
        }

        btnEliminarMateria.setOnClickListener {
            eliminarMateria()
        }

        return view
    }

    private fun verificarYGuardarMateria() {
        val nombreMateria = inputNombreMateria.text.toString()
        val profesorMateria = inputProfesorMateria.text.toString()

        if (nombreMateria.isNotEmpty() && profesorMateria.isNotEmpty()) {
            val userEmail = auth.currentUser?.email

            if (userEmail != null) {
                val materiasCollection = db.collection("users")
                    .document(userEmail)
                    .collection("materias")

                materiasCollection
                    .whereEqualTo("nombre", nombreMateria)
                    .get()
                    .addOnSuccessListener { result ->
                        val materiaDuplicada = result.documents.find { doc ->
                            doc.id != materia?.id
                        }

                        if (materiaDuplicada == null) {
                            guardarMateria(nombreMateria, profesorMateria)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Ya existe una materia con este nombre.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al verificar la materia: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarMateria(nombreMateria: String, profesorMateria: String) {
        val materiaData = hashMapOf(
            "nombre" to nombreMateria,
            "profesor" to profesorMateria
        )

        val userEmail = auth.currentUser?.email

        if (userEmail != null) {
            val materiasCollection = db.collection("users")
                .document(userEmail)
                .collection("materias")

            val documentRef = if (args.isEditMode) {
                materiasCollection.document(materia!!.id)
            } else {
                materiasCollection.document()
            }

            documentRef.set(materiaData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Materia guardada correctamente.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editMateriaFragment_to_materiaFragment)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al guardar la materia: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarMateria() {
        val userEmail = auth.currentUser?.email
        val materiaId = materia?.id

        if (userEmail != null && materiaId != null) {
            db.collection("users")
                .document(userEmail)
                .collection("materias")
                .document(materiaId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Materia eliminada correctamente.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editMateriaFragment_to_materiaFragment)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al eliminar la materia: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Error: No se pudo eliminar la materia.", Toast.LENGTH_SHORT).show()
        }
    }
}
