package com.example.mymindv2.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.mymindv2.HomeActivity
import com.example.mymindv2.MainActivity
import com.example.mymindv2.R
import com.example.mymindv2.adapters.NotificationReceiver
import java.util.Calendar

class NotificationService {
    companion object {
        private const val CHANNEL_ID = "daily_reminder_channel"
        private const val NOTIFICATION_ID = 1001

        // Lista de frases aleatorias para las notificaciones
        private val notificationMessages = listOf(
            "¡Recuerda grabar tus pensamientos hoy! 📢",
            "Un nuevo día, una nueva historia. 🎤",
            "Tómate un momento para hablar. 🗣️",
            "Expresa lo que sientes, es importante. ❤️",
            "Tu voz es poderosa. ¡Úsala! 🎙️",
            "Cada palabra cuenta. ¿Qué dirás hoy? 📝",
            "Un pensamiento al día mantiene la mente en armonía. 🧠",
            "No dejes que el día termine sin compartir algo. ⏳",
            "Registrar tu día puede hacer la diferencia. 📖",
            "Pequeñas palabras, grandes recuerdos. 🗂️"
        )

        fun showAnalysisReadyNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de Análisis",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifica cuando el análisis de voz esté listo."
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("go_to_specific_report", true)
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo2_mymind)
                .setContentTitle("¡Tu reporte está listo! 🎉")
                .setContentText("Toca aquí para ver tu reporte emocional.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(2002, notification)
        }


        fun scheduleNotification(context: Context, hour: Int, minute: Int) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1) // Para que no se active en el pasado
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    context.startActivity(intent)
                    Toast.makeText(context, "Habilita las alarmas exactas en Configuración.", Toast.LENGTH_LONG).show()
                    return
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }

            } catch (e: SecurityException) {
                Toast.makeText(context, "Se necesita permiso para alarmas exactas.", Toast.LENGTH_SHORT).show()

            }
        }

        fun cancelNotification(context: Context) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)

        }

        fun showNotification(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Recordatorio Diario", NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Te recuerda grabar tus pensamientos."
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Seleccionar una frase aleatoria
            val randomMessage = notificationMessages.random()

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo2_mymind) // Ícono completo del logo
                .setContentTitle("Recordatorio Diario")
                .setContentText(randomMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }


}
