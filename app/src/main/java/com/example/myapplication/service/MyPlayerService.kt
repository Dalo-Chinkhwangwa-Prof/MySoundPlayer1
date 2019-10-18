package com.example.myapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.myapplication.R

class MyPlayerService : Service() {

    lateinit var mediaPlayer: MediaPlayer
    private val myBinder = MyPlayerBinder()
    lateinit var notificationManager: NotificationManager

    override fun onBind(intent: Intent): IBinder {
        playPlayer()
        return myBinder
    }

    private fun createNotification(): Notification {
        var notification: Notification?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    "my_channel",
                    "my player channel",
                    NotificationManager.IMPORTANCE_LOW
                )

            notificationManager.createNotificationChannel(notificationChannel)

            notification = Notification.Builder(this, "my_channel")
                .setSmallIcon(R.drawable.ic_play)
                .setTicker("My Sound is Playing")
                .setOngoing(true)
                .build()

        } else {
            notification = Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_play)
                .setTicker("My Sound is Playing")
                .setPriority(Notification.PRIORITY_LOW)
                .setOngoing(true)
                .build()
        }

        return notification
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(1, createNotification())
        mediaPlayer = MediaPlayer.create(this, R.raw.my_sound_file)
        mediaPlayer.isLooping = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer.reset()
        playPlayer()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancel(1)
        sendBroadcast(Intent("com.my.sender").apply {
            this.putExtra("my_key", "STOPPED")
        })
        mediaPlayer.stop()
    }

    fun pausePlayer() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            sendBroadcast(Intent("com.my.sender").apply {
                this.putExtra("my_key", "PAUSED")
            })
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        notificationManager.cancel(1)
    }

    fun playPlayer() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            sendBroadcast(Intent("com.my.sender").apply {
                this.putExtra("my_key", "PLAYING")
            })
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("TAG_X", "unBound!")
        notificationManager.cancel(1)
        sendBroadcast(Intent("com.my.sender").apply {
            this.putExtra("my_key", "STOPPED")
        })
        return true
    }

    inner class MyPlayerBinder : Binder() {
        fun getPlayerService(): MyPlayerService {
            return this@MyPlayerService
        }
    }

}