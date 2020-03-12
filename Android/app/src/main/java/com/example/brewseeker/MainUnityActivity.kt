package com.example.brewseeker

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.example.plugin.OverrideUnityActivity
import com.unity3d.player.UnityPlayer
import java.util.jar.Attributes

class MainUnityActivity :  OverrideUnityActivity() {
    private var playername: String? = "Noplayer"
    private var myUserStatus: String = "Nevahööd"
    // Setup activity layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addControlsToUnityFrame()
        val intent: Intent = intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
        setIntent(intent)
    }

    fun handleIntent(intent: Intent?) {
        if (intent == null || intent.extras == null) return
        if (intent.extras!!.containsKey("doQuit")) if (mUnityPlayer != null) {
            finish()
        }
        if (intent.extras!!.containsKey("playerName")) {
            Log.d("playername extra",intent.extras!!.getString("playerName").toString())
            playername = intent.extras!!.getString("playerName")
        }
    }

    override fun showMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("setUserStatus", myUserStatus)
        startActivity(intent)
    }

    override fun sendDestination(destination: Location) {
    }
    override fun sendCommand(command: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("newCommand", command)
        startActivity(intent)
    }
    override fun onUnityPlayerUnloaded() {
        showMainActivity()
    }
    fun addControlsToUnityFrame() {
        val layout: FrameLayout = mUnityPlayer
        /*run {
            val myButton = Button(this)
            myButton.text = "Map"
            myButton.x = 0f
            myButton.y = 0f
            layout.addView(myButton, 200, 200)
        }*/
        run {
            val myButton = Button(this)
            myButton.text = "MAP"
            myButton.x = 800f
            myButton.y = 0f
            myButton.setOnClickListener { sendCommand("map") }
            layout.addView(myButton, 300, 200)
        }
    }
    fun changePlayerName(){
        UnityPlayer.UnitySendMessage("PlayerName","changeName",playername.toString())
    }

}