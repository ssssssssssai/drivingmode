package com.example.drivingmode

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.*

class DrivingModeService : Service(), TextToSpeech.OnInitListener {

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var textToSpeech: TextToSpeech
    private var isSpeaking = false
    private var dtmfGenerator: DtmfToneGenerator? = null

    companion object {
        private const val CHANNEL_ID = "DrivingModeServiceChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        initializeService()
    }

    private fun initializeService() {
        textToSpeech = TextToSpeech(this, this)
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        dtmfGenerator = DtmfToneGenerator()
        
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        startForegroundService()
    }

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    if (!isSpeaking) {
                        speakAnnouncement(phoneNumber)
                    }
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    isSpeaking = false
                }
            }
        }
    }

    private fun speakAnnouncement(phoneNumber: String?) {
        val message = if (phoneNumber != null) {
            "Incoming call from ${formatPhoneNumber(phoneNumber)}. The person you are calling is driving. If urgent, press 1 to connect."
        } else {
            "The person you are calling is driving. If urgent, press 1 to connect."
        }
        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        isSpeaking = true
    }

    private fun formatPhoneNumber(number: String): String {
        return number.takeLast(4).chunked(2).joinToString(" ")
    }

    private fun startForegroundService() {
        createNotificationChannel()
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Driving Mode Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Driving Mode Service Channel"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val toggleIntent = Intent(this, DrivingModeService::class.java).apply {
            action = "TOGGLE_MODE"
        }
        
        val togglePendingIntent = PendingIntent.getService(
            this,
            0,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Driving Mode Active")
            .setContentText("Incoming calls will be notified that you are driving.")
            .setSmallIcon(R.drawable.ic_driving_mode)
            .setOngoing(true)
            .addAction(0, "Toggle Mode", togglePendingIntent)
            .build()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.US
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        textToSpeech.stop()
        textToSpeech.shutdown()
        dtmfGenerator?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
