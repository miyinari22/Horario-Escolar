package com.miyinari.agendaestudiantil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PerfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        userTextView = view.findViewById(R.id.userTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        btnLogout = view.findViewById(R.id.btnLogout)

        cargarDatosUsuario()

        btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_perfilFragment_to_loginFragment)
        }

        return view
    }

    private fun cargarDatosUsuario() {
        val correoUsuario = auth.currentUser?.email

        if (correoUsuario != null) {
            firestore.collection("users").document(correoUsuario)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombreUsuario = document.getString("usuario") ?: "Usuario no disponible"
                        val correo = document.getString("correo") ?: "Correo no disponible"

                        userTextView.text = nombreUsuario
                        emailTextView.text = correo
                    } else {
                        Toast.makeText(requireContext(), "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al obtener datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
        }
    }
}
