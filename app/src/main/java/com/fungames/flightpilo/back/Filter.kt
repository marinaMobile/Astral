package com.fungames.flightpilo.back

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.fungames.flightpilo.R
import com.fungames.flightpilo.game.Activation
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.*

class Filter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)
        val txtEr: TextView = findViewById(R.id.inviteTxt)
        runBlocking {

            val job: Job = GlobalScope.launch(Dispatchers.IO) {
                getAsync(applicationContext)
            }
            job.join()
            val jsoup: String? = Hawk.get(Cnst.asyncResult, "")
            Log.d("cora", "cora $jsoup")

            txtEr.text = jsoup

            if (jsoup == "b4cS") {
                Intent(applicationContext, Activation::class.java).also { startActivity(it) }
            } else {
                Intent(applicationContext, View::class.java).also { startActivity(it) }
            }
            finish()
        }
    }
    private suspend fun getAsync(context: Context) {
        val asyncKey = JSoup(context)
        val asyncResult = asyncKey.getDocSecretKey()
        Hawk.put(Cnst.asyncResult, asyncResult)
    }
}