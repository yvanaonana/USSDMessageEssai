package com.example.ussdmessageessai

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import android.telecom.TelecomManager
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    val codeOverlay : Int = 3
    val call :Int = 2
    val TAG = "MainActivity"
    lateinit var textDisplay : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn.setOnClickListener {
            process()
        }

//        startService(Intent(this, OverlayService::class.java))


//        val intent = Intent()
//        intent.setAction("message")
//        intent.putExtra("message", "Text")
//        sendBroadcast(intent)

        Log.d(TAG, "in activity")
        val filter = IntentFilter("message")
        this.registerReceiver(Receiver(), filter)
        textDisplay = textView
    }

    private fun process(): Boolean {
        if (!isAccessibilitySettingsOn(this.baseContext)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, 0)
        } else {
            USSDService.pin = "1405"
            USSDService.iTrans = true
            USSDService.iStep = 0
            USSDService.steps = listOf("1", "3", "1", "1", "677277893", "300")
            startService(Intent(this, USSDService::class.java))

            val encodeStart = Uri.encode("#")
            val balance_check = "150"
            val encodedHash = Uri.encode("#")
            val ussd = encodeStart + balance_check + encodedHash

            val telecomManager = baseContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

            val intent = Intent(Intent.ACTION_CALL)//.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) //Intent("android.intent.action.CALL", Uri.parse("tel:$ussd"))
//            intent.putExtra("com.android.phone.extra.slot", 0)
//            intent.putExtra("com.android.phone.force.slot", true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", telecomManager.callCapablePhoneAccounts.get(0))
            }
            intent.setData(Uri.parse("tel:$ussd"))
            startActivityForResult(intent, 1)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_SETTINGS
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.v("permissions", "Permission is granted")

        } else {

            Log.v("permissions", "Permission is revoked")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.READ_PHONE_STATE
                ),
                1
            )

        }
        //        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
        //            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), call)
        //        }

        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {   //Android M Or Over
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            )
            startActivityForResult(intent, codeOverlay)
            return true
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        try {
            this.unregisterReceiver(Receiver())
            stopService(Intent(this, USSDService::class.java))
        }catch (e : Exception){
            e.printStackTrace()
        }
    }


    class Receiver : BroadcastReceiver(){

        val TAG = "USSDService"
        override fun onReceive(context: Context, intent: Intent) {
//            Log.d(TAG, "ici")

            val message = intent.getStringExtra("message")

//            Log.d(TAG, message)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            this.unregisterReceiver(Receiver())
            stopService(Intent(this, USSDService::class.java))
        }catch (e : Exception){

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val dd = data.toString()
                Log.d("myussdmessage", data.toString())
            }
        }
        if (requestCode == 0){
//            Toast.makeText(baseContext, "accessibility", Toast.LENGTH_LONG).show()
//
//            startService(Intent(this, USSDService::class.java))
//
//            val encodeStart = Uri.encode("#")
//            val balance_check = "150*6*2"
//            val encodedHash = Uri.encode("#")
//            val ussd = encodeStart + balance_check + encodedHash
//            startActivityForResult(Intent("android.intent.action.CALL", Uri.parse("tel:$ussd")), 1)
        }
    }

    private fun isAccessibilitySettingsOn(mContext : Context) : Boolean {
        var accessibilityEnabled = 0
        val service = getPackageName() + "/" + USSDService::class.java.getCanonicalName()
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                mContext.getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ACCESSIBILITY_ENABLED)
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled)
        } catch (e : Settings.SettingNotFoundException ) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.message)
        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            val settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service)
                    if (accessibilityService.equals(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!")
                        return true
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***")
        }

        return false
    }
}
