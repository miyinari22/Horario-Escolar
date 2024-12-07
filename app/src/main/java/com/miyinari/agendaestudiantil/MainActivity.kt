package com.miyinari.agendaestudiantil

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.miyinari.agendaestudiantil.data.AppDatabase
import com.miyinari.agendaestudiantil.data.ClassEntity
import com.miyinari.agendaestudiantil.data.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var profileListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showBiometricPrompt()

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(this)
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)

        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        startProfileListener()

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_home)
        }

        createNotificationChannel()

        CoroutineScope(Dispatchers.IO).launch {
            val userEmail = auth.currentUser?.email
            if (userEmail != null) {
                syncDataFromFirestore(database, db, userEmail)
                setupNotifications(this@MainActivity,database, sharedPreferences)
            }
        }

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios"
            val descriptionText = "Canal para recordatorios de tareas y clases"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("TASK_REMINDER_CHANNEL", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            Log.d("Notification", "Canal de notificación creado: ${channel.id}")
        }
    }


    private fun startProfileListener() {
        val userEmail = auth.currentUser?.email
        if (userEmail != null) {
            profileListener = db.collection("users").document(userEmail)
                .addSnapshotListener { documentSnapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val username = documentSnapshot.getString("usuario") ?: "Usuario"
                        val email = documentSnapshot.getString("correo") ?: "usuario@correo.com"
                        val avatarName = documentSnapshot.getString("perfil") ?: "Robot A"
                        updateNavHeader(username, email, avatarName)
                    }
                }
        }
    }

    private fun updateNavHeader(username: String, email: String, avatarName: String) {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)

        val userNameTextView = headerView.findViewById<TextView>(R.id.nav_user_name)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.nav_user_email)
        val avatarImageView = headerView.findViewById<ImageView>(R.id.avatar)

        userNameTextView.text = username
        userEmailTextView.text = email

        val avatarDrawables = mapOf(
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

        val drawableRes = avatarDrawables[avatarName]
        if (drawableRes != null) {
            avatarImageView.setImageResource(drawableRes)
        }
    }

    private suspend fun syncDataFromFirestore(database: AppDatabase, firestore: FirebaseFirestore, userEmail: String) {
        val taskDao = database.taskDao()
        val classDao = database.classDao()

        val userTasksRef = firestore.collection("users").document(userEmail).collection("tareas")
        val userClassesRef = firestore.collection("users").document(userEmail).collection("clases")

        val tasks = userTasksRef.get().await()
        val taskEntities = tasks.documents.mapNotNull { doc ->
            val id = doc.id
            val fecha = doc.getString("fecha")
            val nombre = doc.getString("nombre")
            if (fecha != null && nombre != null) {
                TaskEntity(id, fecha, nombre)
            } else null
        }
        taskDao.deleteAllTasks()
        taskDao.insertTask(taskEntities) // No usar toTypedArray()

        val classes = userClassesRef.get().await()
        val classEntities = classes.documents.mapNotNull { doc ->
            val id = doc.id
            val horaInicio = doc.getString("horaInicio")
            val nombre = doc.getString("materia")
            val dia = doc.getString("dia")
            if (horaInicio != null && nombre != null && dia != null) {
                ClassEntity(id, horaInicio, nombre)
            } else null
        }
        classDao.deleteAllClasses()
        classDao.insertClass(classEntities) // No usar toTypedArray()

        Log.d("FirestoreClassData", "Clases sincronizadas: $classEntities")
    }

    @SuppressLint("ScheduleExactAlarm", "NewApi")
    private suspend fun setupNotifications(
        context: Context,
        database: AppDatabase,
        sharedPreferences: SharedPreferences
    ) {
        val taskDao = database.taskDao()
        val classDao = database.classDao()

        val tasks = taskDao.getAllTasks()
        val classes = classDao.getAllClasses()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val taskReminderDays = sharedPreferences.getInt("taskReminderDays", 1)
        val classReminderMinutes = sharedPreferences.getInt("classReminderMinutes", 15)

        tasks.forEach { task ->
            Log.d("NotificationSetup", "Procesando tarea: ${task.nombre}, Fecha: ${task.fecha}")
            val taskIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("TITLE", "Recordatorio de tarea")
                putExtra("MESSAGE", "Recuerda entregar ${task.nombre} hoy.")
            }
            val taskPendingIntent = PendingIntent.getBroadcast(
                context,
                task.id.hashCode(),
                taskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val taskTime = try {
                LocalDate.parse(task.fecha, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            } catch (e: Exception) {
                Log.e("NotificationSetup", "Error parsing task date: ${task.fecha}", e)
                null
            }

            if (taskTime != null) {
                // Calcula el tiempo de recordatorio en función de los días antes de la tarea
                val reminderTime = taskTime - (taskReminderDays * 24 * 60 * 60 * 1000) // Convertir días a milisegundos
                if (reminderTime > System.currentTimeMillis()) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        taskPendingIntent
                    )
                    Log.d("NotificationSetup", "Task reminder set for: ${task.nombre} at $reminderTime")
                }
            }
        }

        classes.forEach { cl ->
            Log.d("NotificationSetup", "Procesando clase: ${cl.nombre}, Hora inicio: ${cl.horaInicio}")
            val classIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("TITLE", "Recordatorio de clase")
                putExtra("MESSAGE", "Tu clase ${cl.nombre} empieza en ${classReminderMinutes} minutos.")
            }
            val classPendingIntent = PendingIntent.getBroadcast(
                context,
                cl.id.hashCode(),
                classIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val classTime = try {
                val today = LocalDate.now()
                val classTimeParsed = LocalTime.parse(cl.horaInicio, DateTimeFormatter.ofPattern("HH:mm"))
                val classDateTime = LocalDateTime.of(today, classTimeParsed)

                classDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (e: Exception) {
                Log.e("NotificationSetup", "Error parsing class time: ${cl.horaInicio}", e)
                null
            }

            if (classTime != null) {
                val reminderTime = classTime - (classReminderMinutes * 60 * 1000) // Convertir minutos a milisegundos
                if (reminderTime > System.currentTimeMillis()) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        classPendingIntent
                    )
                    Log.d("NotificationSetup", "Class reminder set for: ${cl.nombre} at $reminderTime")
                } else {
                    Log.d("NotificationSetup", "Class '${cl.nombre}' reminder time is in the past: $reminderTime")
                }
            } else {
                Log.d("NotificationSetup", "No se pudo calcular el tiempo de la clase '${cl.nombre}' con hora '${cl.horaInicio}'")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        profileListener?.remove()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> navController.navigate(R.id.horarioFragment)
            R.id.nav_book -> navController.navigate(R.id.materiaFragment)
            R.id.nav_task -> navController.navigate(R.id.tareasFragment)
            R.id.nav_login -> navController.navigate(R.id.loginFragment)
            R.id.nav_config -> navController.navigate(R.id.configuracionFragment)
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Log.d("Biometric", "Autenticación exitosa")
                        // Continúa con el flujo normal de la app
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.e("Biometric", "Error de autenticación: $errString")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Log.e("Biometric", "Autenticación fallida")
                    }
                })

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Autenticación requerida")
                    .setSubtitle("Usa tu huella digital para continuar")
                    .setNegativeButtonText("Cancelar")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("Biometric", "El dispositivo no tiene hardware de biometría")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("Biometric", "El hardware de biometría no está disponible")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.e("Biometric", "El usuario no tiene biometría configurada")
            }
        }
    }
}

