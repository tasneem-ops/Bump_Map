package com.example.mapsdemo.main_screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.mapsdemo.R
import com.example.mapsdemo.authentication.AuthenticationActivity
import com.example.mapsdemo.bluetooth.BluetoothFragment
import com.example.mapsdemo.broadcastReceiver.MyAccessibilityService
import com.example.mapsdemo.settings.SettingsFragment
import com.example.mapsdemo.work.RefreshGeofenceWorker
import com.google.firebase.auth.FirebaseAuth
import java.time.Duration
import java.util.concurrent.TimeUnit



class MainActivity : AppCompatActivity() {
    private var fragment : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = intent.getStringExtra("Fragment").toString()
//        isAccessibilitySettingsOn(applicationContext)


//        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottonnav)
//        bottomNavBar.setOnItemSelectedListener {
//            when(it.itemId){
//                R.id.home ->{
//                    loadFragment(WelcomeFragment())
//                    true
//                }
//                R.id.bluetooth ->{
//                    loadFragment(BluetoothFragment())
//                    true
//                }
//                R.id.map ->{
//                    val intent = MapsActivity.newIntent(applicationContext, null)
//                    startActivity(intent)
//                    true
//                }
//                else->{
//                    true
//                }
//            }
//        }
    }
    private  fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.welcome_fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.logout ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, AuthenticationActivity::class.java)
                startActivity(intent)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

//
//        private val ACCESSIBILITY_ENABLED = 1
//
//        fun isAccessibilitySettingsOn(context: Context): Boolean {
//            var accessibilityEnabled = 0
//            val service: String = context.getPackageName()
//                .toString() + "/" + MyAccessibilityService::class.java.getCanonicalName()
//            try {
//                accessibilityEnabled = Settings.Secure.getInt(
//                    context.getApplicationContext().getContentResolver(),
//                    Settings.Secure.ACCESSIBILITY_ENABLED
//                )
//            } catch (e: Settings.SettingNotFoundException) {
//                Log.e(
//                    "AU", "Error finding setting, default accessibility to not found: "
//                            + e.message.toString()
//                )
//            }
//            val mStringColonSplitter = SimpleStringSplitter(':')
//            if (accessibilityEnabled == ACCESSIBILITY_ENABLED) {
//                val settingValue: String = Settings.Secure.getString(
//                    context.getApplicationContext().getContentResolver(),
//                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
//                )
//                if (settingValue != null) {
//                    mStringColonSplitter.setString(settingValue)
//                    while (mStringColonSplitter.hasNext()) {
//                        val accessibilityService = mStringColonSplitter.next()
//                        if (accessibilityService.equals(service, ignoreCase = true)) {
//                            return true
//                        }
//                    }
//                }
//            }
//            return false
//        }

    fun getSelectedFragment(): String?{
        return fragment
    }

    fun setSelectedFragment(fragment: String){
        this.fragment = fragment
    }
}