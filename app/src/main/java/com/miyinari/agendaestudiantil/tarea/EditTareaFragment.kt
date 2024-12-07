package com.miyinari.agendaestudiantil.tarea

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miyinari.agendaestudiantil.R
import java.util.*

class EditTareaFragment : Fragment() {

    private lateinit var etNombreTarea: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnFechaEntrega: Button
    private lateinit var btnHoraEntrega: Button
    private lateinit var btnGuardarTarea: Button
    private lateinit var btnEliminarTarea: Button
    private lateinit var btnSeleccionarMateria: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var tarea: Tarea
    private var materiasList: List<String> = emptyList()
    private var selectedMateria: String? = null

    private val args: EditTareaFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_tarea, container, false)

        etNombreTarea = view.findViewById(R.id.etNombreTarea)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        btnSeleccionarMateria = view.findViewById(R.id.btnSeleccionarMateria)
        btnFechaEntrega = view.findViewById(R.id.btnFechaEntrega)
        btnHoraEntrega = view.findViewById(R.id.btnHoraEntrega)
        btnGuardarTarea = view.findViewById(R.id.btnGuardarClase)
        btnEliminarTarea = view.findViewById(R.id.btnEliminarClase)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        cargarMaterias()
        tarea = args.tarea
        cargarTarea(tarea)

        if (args.isEditMode) {
            btnEliminarTarea.visibility = View.VISIBLE
        } else {
            btnEliminarTarea.visibility = View.GONE
        }

        btnFechaEntrega.setOnClickListener { mostrarCalendario() }
        btnHoraEntrega.setOnClickListener { mostrarReloj() }
        btnGuardarTarea.setOnClickListener {
            if (args.isEditMode) {
                verificarYGuardarTarea(tarea.id)
            } else {
                verificarYGuardarTarea()
            }
        }
        btnEliminarTarea.setOnClickListener { eliminarTarea(tarea.id) }
        btnSeleccionarMateria.setOnClickListener { mostrarDialogoSeleccionMateria() }

        return view
    }

    private fun cargarMaterias() {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("users")
                .document(userEmail)
                .collection("materias")
                .get()
                .addOnSuccessListener { result ->
                    materiasList = result.mapNotNull { it.getString("nombre") }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al cargar las materias: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarDialogoSeleccionMateria() {
        if (materiasList.isNotEmpty()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Selecciona una Materia")
            builder.setSingleChoiceItems(materiasList.toTypedArray(), -1) { dialog, which ->
                selectedMateria = materiasList[which]
                btnSeleccionarMateria.text = selectedMateria ?: "Seleccionar Materia"
                dialog.dismiss()
            }
            builder.create().show()
        } else {
            Toast.makeText(requireContext(), "No hay materias disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarTarea(tarea: Tarea) {
        etNombreTarea.setText(tarea.nombre)
        etDescripcion.setText(tarea.descripcion)
        btnFechaEntrega.text = tarea.fecha.ifEmpty { "Seleccionar Fecha de Entrega" }
        btnHoraEntrega.text = tarea.hora.ifEmpty { "Seleccionar Hora de Entrega" }
        selectedMateria = tarea.materias
        btnSeleccionarMateria.text = selectedMateria ?: "Seleccionar Materia"
    }

    private fun mostrarCalendario() {
        val calendario = Calendar.getInstance()
        val dia = calendario.get(Calendar.DAY_OF_MONTH)
        val mes = calendario.get(Calendar.MONTH)
        val anio = calendario.get(Calendar.YEAR)

        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            val fecha = "$dayOfMonth/${month + 1}/$year"
            btnFechaEntrega.text = fecha
        }, anio, mes, dia).show()
    }

    private fun mostrarReloj() {
        val calendario = Calendar.getInstance()
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            val horaEntrega = String.format("%02d:%02d", hourOfDay, minute)
            btnHoraEntrega.text = horaEntrega
        }, hora, minuto, true).show()
    }


    private fun verificarYGuardarTarea(tareaId: String? = null) {
        val nombre = etNombreTarea.text.toString()
        val descripcion = etDescripcion.text.toString()
        val fecha = btnFechaEntrega.text.toString()
        val hora = btnHoraEntrega.text.toString()
        val materiaNombre = selectedMateria

        if (nombre.isNotEmpty() && descripcion.isNotEmpty() && materiaNombre != null) {
            val userEmail = auth.currentUser?.email

            if (userEmail != null) {
                val tareasCollection = db.collection("users")
                    .document(userEmail)
                    .collection("tareas")

                val query = tareasCollection
                    .whereEqualTo("nombre", nombre)
                    .whereEqualTo("materias", materiaNombre)

                query.get()
                    .addOnSuccessListener { result ->
                        val tareaDuplicada = result.documents.find { doc ->
                            doc.id != tareaId
                        }

                        if (tareaDuplicada == null) {
                            guardarTarea(nombre, descripcion, fecha, hora, materiaNombre, tareaId)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Ya existe una tarea con el mismo nombre para esta materia.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al verificar la tarea: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarTarea(
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        materiaNombre: String,
        tareaId: String? = null
    ) {
        val userEmail = auth.currentUser?.email
        val tareaData = hashMapOf(
            "nombre" to nombre,
            "descripcion" to descripcion,
            "fecha" to fecha,
            "hora" to hora,
            "materias" to materiaNombre
        )

        if (userEmail != null) {
            val tareasCollection = db.collection("users")
                .document(userEmail)
                .collection("tareas")

            if (tareaId == null) {
                tareasCollection.add(tareaData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Tarea guardada correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_editTareaFragment_to_tareaFragment)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al guardar la tarea: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                tareasCollection.document(tareaId).set(tareaData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Tarea actualizada correctamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_editTareaFragment_to_tareaFragment)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al actualizar la tarea: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun eliminarTarea(tareaId: String?) {
        val userEmail = auth.currentUser?.email

        if (userEmail != null && tareaId != null) {
            db.collection("users")
                .document(userEmail)
                .collection("tareas")
                .document(tareaId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editTareaFragment_to_tareaFragment)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al eliminar la tarea: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Error: No se pudo obtener el correo del usuario o el ID de la tarea", Toast.LENGTH_SHORT).show()
        }
    }
}
