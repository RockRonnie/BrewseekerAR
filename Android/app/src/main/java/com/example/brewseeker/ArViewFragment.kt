package com.example.brewseeker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.brewseeker.MainUnityActivity
import com.example.brewseeker.R
import kotlinx.android.synthetic.main.ar_fragment.*
import kotlinx.android.synthetic.main.ar_fragment.view.*
import java.lang.Exception

class ArViewFragment: Fragment() {
    companion object {

        private const val AR = "ar"

        fun newInstance(playerStatus: String): ArViewFragment {
            val args = Bundle()
            args.putSerializable(AR,playerStatus)
            val fragment = ArViewFragment()
            fragment.arguments = args
            return fragment
        }
    }
    lateinit var ctx: Context
    private lateinit var listener: UnityRunning

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        ctx = activity as Context
        val view = inflater.inflate(R.layout.ar_fragment ,container, false )
        val model = arguments!!.getSerializable(AR)
        Log.d("Status text",model.toString())
        view.status.text = model.toString()
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startButton.setOnClickListener{
            startGame()
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UnityRunning) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement UnityRUnning.")
        }
    }
    private fun validate(): Boolean {
        return playerInput.text != null
    }
    private fun startGame(){
        if(validate()){
            Log.d("playerInput", playerInput.text.toString())
            try {
                Log.d("main", "Starting unity")
                listener.unityIsRunning(true)
                val intent = Intent(ctx, MainUnityActivity::class.java)
                intent.putExtra("playerName", playerInput.text.toString())
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivityForResult(intent, 1)
            }catch(e: Exception){
                Log.e("Main", e.toString())
            }
        }else{
            showToast("Please insert a playername!")
        }
    }
    fun showToast(message: String) {
        val text: CharSequence = message
        val duration = Toast.LENGTH_SHORT
        val toast = Toast.makeText(ctx, text, duration)
        toast.show()
    }
    interface UnityRunning {
        fun unityIsRunning(unity: Boolean)
    }
}