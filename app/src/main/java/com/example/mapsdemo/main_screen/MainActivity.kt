package com.example.mapsdemo.main_screen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.mapsdemo.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.example.mapsdemo.authentication.AuthenticationActivity
import com.example.mapsdemo.bluetooth.BluetoothFragment
import com.example.mapsdemo.map_screen.MapsActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



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
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.myNavHostFragment,fragment)
        transaction.commit()
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
}