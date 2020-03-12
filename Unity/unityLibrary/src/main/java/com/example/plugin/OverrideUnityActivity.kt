package com.example.plugin
import android.location.Location
import android.os.Bundle
import android.widget.FrameLayout
import com.unity3d.player.UnityPlayerActivity


abstract class OverrideUnityActivity : UnityPlayerActivity() {

    protected abstract fun showMainActivity()
    protected abstract fun sendDestination(destination: Location)
    protected abstract fun sendCommand(command: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    companion object {
        var instance: OverrideUnityActivity? = null
    }
}