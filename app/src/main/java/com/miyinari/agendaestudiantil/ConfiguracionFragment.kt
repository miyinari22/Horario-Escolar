package com.miyinari.agendaestudiantil

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ConfiguracionFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var switchClase: Switch
    private lateinit var switchTarea: Switch
    private lateinit var btnClaseNotif: Button
    private lateinit var btnTareaSemana: Button
    private lateinit var btnTareaHora: Button

    private val avatarOptions = arrayOf("Abuelo", "Abuela", "Niño", "Niña", "Hombre", "Mujer", "Robot A", "Robot B", "Robot C")
    private val avatarDrawables = mapOf(
        "Abuelo" to R.drawable.abuelo,
        "Abuela" to R.drawable.abuela,
        "Niño" to R.drawable.nino,
        "Niña" to R.drawable.nina,
        "Hombre" to R.drawable.hombre,
        "Mujer" to R.drawable.mujer,
        "Robot A" to R.drawable.robot_a,
        "Robot B" to R.drawable.robot_b,
        "Robot C" to R.drawable.robot_c
    )
    private var selectedAvatar: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_configuracion, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        sharedPreferences = requireContext().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        profileImageView = view.findViewById(R.id.profile_image)
        val changeAvatarButton: Button = view.findViewById(R.id.button)
        val saveChangesButton: Button = view.findViewById(R.id.btnChange_profile)
        switchClase = view.findViewById(R.id.switchClase)
        switchTarea = view.findViewById(R.id.switchTarea)
        btnClaseNotif = view.findViewById(R.id.claseNotif)
        btnTareaSemana = view.findViewById(R.id.tareaSemana)
        btnTareaHora = view.findViewById(R.id.tareaHora)

        val savedAvatar = sharedPreferences.getString("perfil", "Robot A")
        updateProfileImage(savedAvatar)

        switchClase.isChecked = sharedPreferences.getBoolean("switchClase", false)
        switchTarea.isChecked = sharedPreferences.getBoolean("switchTarea", false)

        updateNotificationControlsVisibility()

        changeAvatarButton.setOnClickListener { showAvatarDialog() }
        saveChangesButton.setOnClickListener { saveConfiguration() }

        switchClase.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("switchClase", isChecked)
                .apply()
            btnClaseNotif.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        switchTarea.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit()
                .putBoolean("switchTarea", isChecked)
                .apply()
            val visibility = if (isChecked) View.VISIBLE else View.GONE
            btnTareaSemana.visibility = visibility
            btnTareaHora.visibility = visibility
        }

        setupReminderControls()

        checkExactAlarmPermission()

        return view
    }

    private fun saveConfiguration() {
        sharedPreferences.edit()
            .putBoolean("switchClase", switchClase.isChecked)
            .putBoolean("switchTarea", switchTarea.isChecked)
            .apply()

        Toast.makeText(requireContext(), "Configuración guardada", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_configuracionFragment_to_horarioFragment)
    }

    private fun updateNotificationControlsVisibility() {
        if (!switchClase.isChecked) {
            cancelClassNotification()
        }
        if (!switchTarea.isChecked) {
            cancelTaskNotifications()
        }

        btnClaseNotif.visibility = if (switchClase.isChecked) View.VISIBLE else View.GONE
        val tareaVisibility = if (switchTarea.isChecked) View.VISIBLE else View.GONE
        btnTareaSemana.visibility = tareaVisibility
        btnTareaHora.visibility = tareaVisibility
    }

    private fun cancelClassNotification() {
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun cancelTaskNotifications() {
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!isExactAlarmPermissionGranted()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun isExactAlarmPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun showAvatarDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona un avatar")
            .setItems(avatarOptions) { _, which ->
                selectedAvatar = avatarOptions[which]
                updateProfileImage(selectedAvatar)
            }
            .show()
    }

    private fun updateProfileImage(avatarName: String?) {
        avatarName?.let {
            val drawableRes = avatarDrawables[it]
            if (drawableRes != null) {
                profileImageView.setImageResource(drawableRes)
                sharedPreferences.edit().putString("perfil", it).apply()
            }
        }
    }

    private fun setupReminderControls() {
        btnClaseNotif.setOnClickListener { showClassReminderDialog() }
        btnTareaSemana.setOnClickListener { showTaskDayReminderDialog() }
        btnTareaHora.setOnClickListener { showTaskTimeReminderDialog() }
    }

    private fun showClassReminderDialog() {
        val options = arrayOf("5 segundos", "5 minutos", "10 minutos", "15 minutos")
        AlertDialog.Builder(requireContext())
            .setTitle("Configura recordatorio para clases")
            .setItems(options) { _, which ->
                val minutesBefore = when (which) {
                    0 -> 0 // 5 segundos antes
                    1 -> 5
                    2 -> 10
                    3 -> 15
                    else -> 5
                }
                sharedPreferences.edit()
                    .putInt("classReminderMinutes", minutesBefore)
                    .apply()

                Toast.makeText(requireContext(), "Notificación configurada para $minutesBefore minutos antes", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showTaskDayReminderDialog() {
        val options = arrayOf("Mismo día", "Un día antes")
        AlertDialog.Builder(requireContext())
            .setTitle("Configura días para tareas")
            .setItems(options) { _, which ->
                val daysBefore = if (which == 0) 0 else 1
                sharedPreferences.edit()
                    .putInt("taskReminderDays", daysBefore)
                    .apply()

                val dayText = if (daysBefore == 0) "Mismo día" else "Un día antes"
                Toast.makeText(requireContext(), "Notificaciones configuradas para $dayText", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showTaskTimeReminderDialog() {
        val calendario = Calendar.getInstance()
        val hora = calendario.get(Calendar.HOUR_OF_DAY)
        val minuto = calendario.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val formattedHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
            val horaEntrega = String.format("%02d:%02d %s", formattedHour, minute, amPm)

            btnTareaHora.text = horaEntrega

            sharedPreferences.edit()
                .putString("horaEntrega", horaEntrega)
                .apply()

            Toast.makeText(requireContext(), "Hora de tarea configurada a las $horaEntrega", Toast.LENGTH_SHORT).show()
        }, hora, minuto, false).show()
    }
}
