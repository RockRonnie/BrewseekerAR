package com.example.brewseeker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.ar_fragment.*


class MainActivity : AppCompatActivity(), ArViewFragment.UnityRunning {
    private lateinit var mapViewFragment: MapViewFragment
    private lateinit var arViewFragment: ArViewFragment
    private lateinit var myManager: FragmentManager
    private lateinit var myTransaction: FragmentTransaction

    var isUnityLoaded = false
    lateinit var toggle: Button
    private var state: String = "map"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntent(intent)
        toggle = findViewById<Button>(R.id.toggleButton)
        toggle.setOnClickListener {
            changeFragment(state)
        }
        if (savedInstanceState == null) {
            changeFragment(state)
        }
    }
    private fun toggleState(newState: String){
        state = newState
        toggle.text = newState
    }
    private fun changeFragment(myState: String){
        when(myState){
            "map" -> {
                toggleState("ar")
                mapViewFragment = MapViewFragment.newInstance()
                myManager = supportFragmentManager
                myTransaction = myManager.beginTransaction()
                myTransaction.add(R.id.root_frame, mapViewFragment)
                myTransaction.commit()
            }
            "ar" -> {
                toggleState("map")
                arViewFragment = ArViewFragment.newInstance("Player status: Nevahööd")
                myManager = supportFragmentManager
                myTransaction = myManager.beginTransaction()
                myTransaction.add(R.id.root_frame, arViewFragment)
                myTransaction.commit()
            }
        }
    }
    fun handleIntent(intent: Intent?) {
        if (intent == null || intent.extras == null) return
        if(intent.extras!!.containsKey("newCommand")){
            when (intent.extras!!.getString("newCommand")){
                "map" -> {changeFragment("map")}
                "ar" -> {changeFragment("ar")}
                else -> {
                    showToast("No command")
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) isUnityLoaded = false
    }
    fun showToast(message: String) {
        val text: CharSequence = message
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }
    override fun onBackPressed() {
        finishAffinity()
    }

    override fun unityIsRunning(unity: Boolean) {
        isUnityLoaded = unity
    }
}