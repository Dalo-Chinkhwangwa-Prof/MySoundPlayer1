package com.example.myapplication.view

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.service.MyPlayerService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var myPlayerService: MyPlayerService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName) {
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            myPlayerService = (iBinder as MyPlayerService.MyPlayerBinder).getPlayerService()
        }
    }

    val handler = Handler()

    private val myReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.getStringExtra("my_key")?.let { mediaStatus ->
                handler.post {
                    media_status_textvuew.text = mediaStatus
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(myReceiver, IntentFilter("com.my.sender"))

        val intent = Intent(this, MyPlayerService::class.java)

        play_button.setOnClickListener {
            myPlayerService?.playPlayer() ?: {
                startService(intent)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }()
        }

        pause_button.setOnClickListener {
            myPlayerService?.pausePlayer()
        }

        stop_button.setOnClickListener {
            if (myPlayerService != null) {
                unbindService(serviceConnection)
                myPlayerService = null
            }

//            stopService(intent) //will stop when we close.....
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myReceiver)
    }

}
