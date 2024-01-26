package com.example.ter

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity
import com.example.ter.databinding.SettingsBinding // Замените на вашу связку для SettingsActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsBinding // Замените на вашу связку для SettingsActivity
    private lateinit var pref: SharedPreferences
    private val client = OkHttpClient()
    private lateinit var request: Request

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsBinding.inflate(layoutInflater) // Замените на вашу связку для SettingsActivity
        setContentView(binding.root)
        pref = getSharedPreferences("MyPref", MODE_PRIVATE)
        onClickSaveIp()
        getIp()
        onClickbuttonTimeOk()
        onClickbuttonTimeOk2()
        onClickbuttonTimeCancel()
        onClickbuttonTimeCancel2()
    }

    private fun getIp() = with(binding) {
        val ip = pref.getString("ip", "")
        if (ip != null) {
            if (ip.isNotEmpty()) edIpS.setText(ip)
        }
    }

    private fun onClickSaveIp() = with(binding) {
        bSaveS.setOnClickListener {
            vibrate()
            if (edIpS.text.isNotEmpty()) saveIp(edIpS.text.toString())
        }
    }

    private fun onClickbuttonTimeCancel() = with(binding) {
        onClickbuttonTimeCancel.setOnClickListener {
            vibrate()
            var selected = spinner.selectedItem
            var id = -1
            val ip = pref.getString("ip", "")
            when (selected) {
                "Свет и подогрев" -> id = 2
                "Туманогенратор" -> id = 1
                "Помпа" -> id = 0
            }
            for(i in 0..2) {
                var req = "http://$ip/relay?id=$i&hourin=-1&minin=-1&hourout=-1&minout=-1"
                println(req)
                post(req)
            }

        }
    }

    private fun onClickbuttonTimeCancel2() = with(binding) {
        onClickbuttonTimeCancel2.setOnClickListener {
            vibrate()
            var selected = spinner.selectedItem
            var id = -1
            val ip = pref.getString("ip", "")
            when (selected) {
                "Свет и подогрев" -> id = 2
                "Туманогенратор" -> id = 1
                "Помпа" -> id = 0
            }
            var req = "http://$ip/everyhour?id=$id&status=0&timein=${editEveryhourstart.text}&timeout=${editEveryhourend.text}&time=${editEveryhour.text}"
            post(req)
        }
    }

    private fun onClickbuttonTimeOk() = with(binding) {
        buttonTimeOk.setOnClickListener {
            vibrate()
            var selected = spinner.selectedItem
            var id = -1
            val ip = pref.getString("ip", "")
            when (selected) {
                "Свет и подогрев" -> id = 2
                "Туманогенратор" -> id = 1
                "Помпа" -> id = 0
            }
            var req = "http://$ip/relay?id=$id&hourin=${editHourin.text}&minin=${editMinin.text}&hourout=${editHourout.text}&minout=${editMinout.text}"
            post(req)
        }
    }

    private fun onClickbuttonTimeOk2() = with(binding) {
        buttonTimeOk2.setOnClickListener {
            vibrate()
            var selected = spinner2.selectedItem
            var id = -1
            val ip = pref.getString("ip", "")
            when (selected) {
                "Свет и подогрев" -> id = 2
                "Туманогенратор" -> id = 1
                "Помпа" -> id = 0
            }
            var req = "http://$ip/everyhour?id=$id&status=1&timein=${editEveryhourstart.text}&timeout=${editEveryhourend.text}&time=${editEveryhour.text}"
            println(req)
            post(req)
        }
    }


    private fun post(req: String){
        Thread{

            request = Request.Builder().url(req).build()
            try {
                var response = client.newCall(request).execute()
                if(response.isSuccessful){
                    val resultText = response.body()?.string()
                }
            } catch (i: IOException){

            }
        }.start()
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

    private fun saveIp(ip: String) {
        val editor = pref.edit()
        editor.putString("ip", ip)
        editor.apply()
    }
}
