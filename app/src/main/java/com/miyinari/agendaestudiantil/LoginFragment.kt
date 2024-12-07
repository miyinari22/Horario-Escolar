package com.miyinari.agendaestudiantil

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_perfilFragment)
            return view
        }

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        btnLogin = view.findViewById(R.id.btnLogin)
        btnRegister = view.findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener { iniciarSesion() }
        btnRegister.setOnClickListener { registrarUsuario() }

        return view
    }

    private fun registrarUsuario() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val perfil = "Robot A"

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = hashMapOf(
                            "nombre" to name,
                            "correo" to email,
                            "perfil" to perfil
                        )

                        db.collection("users").document(email)
                            .set(user)
                            .addOnSuccessListener {
                                guardarDatosEnSharedPreferences(name, email, perfil)
                                Toast.makeText(requireContext(), "Registro exitoso.", Toast.LENGTH_SHORT).show()

                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Error al guardar datos en Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun iniciarSesion() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.collection("users").document(email)
                            .get()
                            .addOnSuccessListener { document ->
                                val perfil = document.getString("perfil") ?: "Robot A"
                                guardarDatosEnSharedPreferences(document.getString("nombre") ?: "", email, perfil)
                                findNavController().navigate(R.id.action_loginFragment_to_perfilFragment)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Error al recuperar datos del usuario", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarDatosEnSharedPreferences(name: String, email: String, perfil: String) {
        val editor = sharedPreferences.edit()
        editor.putString("usuario", name)
        editor.putString("correo", email)
        editor.putString("perfil", perfil)
        editor.apply()
    }
}
