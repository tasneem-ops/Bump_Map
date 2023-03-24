package com.example.mapsdemo.main_screen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mapsdemo.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.example.mapsdemo.bluetooth.BluetoothFragment
import com.example.mapsdemo.map_screen.MapsActivity

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
}