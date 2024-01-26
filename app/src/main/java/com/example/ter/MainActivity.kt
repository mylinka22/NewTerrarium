package com.example.ter;
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.ter.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {
    private lateinit var request: Request
    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: SharedPreferences
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = getSharedPreferences("MyPref", MODE_PRIVATE)
        getIp()
        binding.apply {
            bLed1.setOnClickListener(onClickListener())
            bLed2.setOnClickListener(onClickListener())
            bLed3.setOnClickListener(onClickListener())
            settingsBut.setOnClickListener(settingsintent())
//            tvTemp.setOnClickListener(temppost())
//            tvHumidity.setOnClickListener(temppost())
        }
        posttemp("temperature")
        refreshApp()
    }


    override fun onResume() {
        super.onResume()
        getIp()
    }

    private fun vibrate(){
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.EFFECT_HEAVY_CLICK) )
        }else{
            @Suppress("DEPRECATION")
            vib.vibrate(100)
        }
    }



    private fun refreshApp() = with(binding) {

        swiperefresh.setOnRefreshListener {
//            Toast.makeText(this@MainActivity, "121233", Toast.LENGTH_SHORT).show()
            vibrate()
            posttemp("temperature")
            swiperefresh.isRefreshing = false

        }

    }

    private fun temppost(): View.OnClickListener{
        return View.OnClickListener {
            posttemp("temperature")
        }
    }

    private fun settingsintent(): View.OnClickListener{
        return View.OnClickListener {
            vibrate()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onClickListener(): View.OnClickListener{
        return View.OnClickListener {
            vibrate()
            when(it.id){
                R.id.bLed1 -> { post("led1") }
                R.id.bLed2 -> { post("led2") }
                R.id.bLed3 -> { post("led3") }
            }
        }
    }

    private fun getIp() = with(binding){
        val ip = pref.getString("ip", "")
        if(ip != null){
            if(ip.isNotEmpty()) edIp.setText(ip)
        }
    }

    private fun saveIp(ip: String){
        val editor = pref.edit()
        editor.putString("ip", ip)
        editor.apply()
    }

    private fun post(post: String){
        Thread{


            request = Request.Builder().url("http://${binding.edIp.text}/$post").build()
            try {
                var response = client.newCall(request).execute()
            } catch (i: IOException){

            }

        }.start()
    }


    private fun posttemp(post: String) {
        Thread {
            val url = "http://${binding.edIp.text}/$post"
            val request = Request.Builder().url(url).build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val resultText = response.body()?.string()

                    val parts = resultText?.split("\\s+".toRegex())

                    var datchikt: String? = null
                    var datchikh: String? = null

                    parts?.forEachIndexed { index, part ->
                        when {
                            part.contains("Datchikt:") -> datchikt = parts.getOrNull(index + 1)
                            part.contains("Datchikh:") -> datchikh = parts.getOrNull(index + 1)
                        }
                    }

                    runOnUiThread {
                        binding.tvTemp.text = datchikt + "Cº"
                        binding.tvHumidity.text = datchikh + "%"
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

}