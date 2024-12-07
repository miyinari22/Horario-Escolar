package com.miyinari.agendaestudiantil.clase

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.miyinari.agendaestudiantil.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class ClaseFragment : Fragment() {

    private lateinit var etSalon: EditText
    private lateinit var etColor: EditText
    private lateinit var btnSeleccionarMateria: Button
    private lateinit var btnDia: Button
    private lateinit var btnHoraInicio: Button
    private lateinit var btnHoraFin: Button
    private lateinit var btnGuardarClase: Button
    private lateinit var btnEliminarClase: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedMateria: String? = null
    private var selectedDia: String? = null
    private var materiasList: List<String> = emptyList()

    private val args: ClaseFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clase, container, false)

        etSalon = view.findViewById(R.id.etSalon)
        etColor = view.findViewById(R.id.etColor)
        btnSeleccionarMateria = view.findViewById(R.id.btnSeleccionarMateria)
        btnDia = view.findViewById(R.id.btnDia)
        btnHoraInicio = view.findViewById(R.id.btnHoraIncio)
        btnHoraFin = view.findViewById(R.id.btnHoraFin)
        btnGuardarClase = view.findViewById(R.id.btnGuardarClase)
        btnEliminarClase = view.findViewById(R.id.btnEliminarClase)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        cargarMaterias()

        val clase = args.clase

        if (clase.id.isNotEmpty()) {
            cargarClase(clase.id)
        }

        args.isEditMode
        if (args.isEditMode) {
            btnEliminarClase.visibility = View.VISIBLE
        } else {
            btnEliminarClase.visibility = View.GONE
        }

        btnSeleccionarMateria.setOnClickListener { mostrarDialogoSeleccionMateria() }
        btnDia.setOnClickListener { mostrarDialogoSeleccionDia() }
        btnHoraInicio.setOnClickListener { mostrarReloj(btnHoraInicio) }
        btnHoraFin.setOnClickListener { mostrarReloj(btnHoraFin) }
        btnGuardarClase.setOnClickListener { verificarConflictoYGuardar() }
        btnEliminarClase.setOnClickListener { eliminarClase(clase.id) }

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

    private fun cargarClase(claseId: String) {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("users")
                .document(userEmail)
                .collection("clases")
                .document(claseId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val clase = document.toObject(Clase::class.java)
                        clase?.let {
                            etSalon.setText(it.salon)
                            etColor.setText(it.color)

                            val horaInicio24 = convertirAFormato24Horas(it.horaInicio)
                            val horaFin24 = convertirAFormato24Horas(it.horaFin)

                            btnHoraInicio.text = horaInicio24
                            btnHoraFin.text = horaFin24

                            btnDia.text = it.dia
                            selectedMateria = it.materia
                            btnSeleccionarMateria.text = selectedMateria ?: "Seleccionar Materia"
                        }
                    } else {
                        Toast.makeText(requireContext(), "Clase no encontrada.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al cargar la clase: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertirAFormato24Horas(hora: String): String {
        return try {
            val format12 = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())  // Formato AM/PM
            val format24 = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())    // Formato 24 horas
            val horaLocal = LocalTime.parse(hora, format12)
            horaLocal.format(format24)
        } catch (e: Exception) {
            hora
        }
    }


    private fun mostrarDialogoSeleccionMateria() {
        if (materiasList.isNotEmpty()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Selecciona una Materia")
            builder.setSingleChoiceItems(materiasList.toTypedArray(), -1) { dialog, which ->
                selectedMateria = materiasList[which]
                btnSeleccionarMateria.text = selectedMateria
                dialog.dismiss()
            }
            builder.create().show()
        } else {
            Toast.makeText(requireContext(), "No hay materias disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoSeleccionDia() {
        val dias = arrayOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Selecciona un Día")
        builder.setSingleChoiceItems(dias, -1) { dialog, which ->
            selectedDia = dias[which]
            btnDia.text = selectedDia
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun mostrarReloj(button: Button) {
        val calendario = Calendar.getInstance()
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            val horaSeleccionada = String.format("%02d:%02d", hourOfDay, minute)
            button.text = horaSeleccionada
        }, hora, minuto, true).show()
    }


    private fun verificarConflictoYGuardar() {
        val userEmail = auth.currentUser?.email
        val materiaSeleccionada = selectedMateria
        val diaSeleccionado = selectedDia
        val horaInicio = btnHoraInicio.text.toString()

        if (userEmail != null && materiaSeleccionada != null && diaSeleccionado != null) {
            db.collection("users")
                .document(userEmail)
                .collection("clases")
                .whereEqualTo("dia", diaSeleccionado)
                .whereEqualTo("horaInicio", horaInicio)
                .get()
                .addOnSuccessListener { documents ->
                    var conflicto = false

                    for (document in documents) {
                        val claseExistente = document.toObject(Clase::class.java)
                        if (claseExistente.materia == materiaSeleccionada) {
                            conflicto = true
                            Toast.makeText(requireContext(), "Clase existente", Toast.LENGTH_SHORT).show()
                            break
                        } else {
                            conflicto = true
                            Toast.makeText(requireContext(), "Conflicto de hora con clase ya existente", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }

                    if (!conflicto) {
                        guardarClase()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al verificar conflictos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarClase() {
        val salon = etSalon.text.toString()
        var color = etColor.text.toString().takeIf { it.isNotBlank() } ?: "#FFFFFF"
        val horaInicio = btnHoraInicio.text.toString()
        val horaFin = btnHoraFin.text.toString()
        val diaSeleccionado = btnDia.text.toString()
        val materiaSeleccionada = selectedMateria

        if (salon.isNotEmpty() && diaSeleccionado.isNotEmpty() && materiaSeleccionada != null) {
            val userEmail = auth.currentUser?.email

            if (userEmail != null) {
                val claseId = args.clase.id.takeIf { it.isNotEmpty() } ?: UUID.randomUUID().toString()

                val clase = Clase(
                    id = claseId,
                    salon = salon,
                    color = color,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    dia = diaSeleccionado,
                    materia = materiaSeleccionada
                )

                db.collection("users")
                    .document(userEmail)
                    .collection("clases")
                    .document(claseId)
                    .set(clase)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Clase guardada correctamente.", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_claseFragment_to_horarioFragment)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al guardar la clase: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eliminarClase(id: String) {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            db.collection("users")
                .document(userEmail)
                .collection("clases")
                .document(id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Clase eliminada correctamente.", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_claseFragment_to_horarioFragment)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al eliminar la clase: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
