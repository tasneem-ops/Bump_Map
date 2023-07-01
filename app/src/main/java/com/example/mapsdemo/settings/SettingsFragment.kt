package com.example.mapsdemo.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.mapsdemo.R
import com.example.mapsdemo.bluetooth.BluetoothFragmentDirections
import com.example.mapsdemo.databinding.FragmentSettingsBinding
import com.example.mapsdemo.map_screen.MapsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.*

class SettingsFragment : Fragment() {
        private lateinit var binding : FragmentSettingsBinding
        private var sensitivity : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        binding.saveBtn.setOnClickListener {
            val distance = binding.distanceEditText.text.toString()
            val sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
            sharedPreferences.edit().putInt(getString(R.string.shared_pref_distance_key), distance.toInt()).commit()
        }

        binding.testBtn.setOnClickListener {
            val sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.app_name),
                Context.MODE_PRIVATE)
            val distance = sharedPreferences.getInt(getString(R.string.shared_pref_distance_key), Context.MODE_PRIVATE)
            Toast.makeText(requireActivity(), "Distance Saved : $distance", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNavBar = view?.findViewById<BottomNavigationView>(R.id.bottonnav)
        bottomNavBar?.selectedItemId = R.id.settings
        bottomNavBar?.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToWelcomeFragment())
                    true
                }
                R.id.bluetooth ->{
                    findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToBluetoothFragment())
                    true
                }
                R.id.map_item ->{
                    val intent = MapsActivity.newIntent(requireContext(), null)
                    startActivity(intent)
                    true
                }
                R.id.bluetooth ->{
                    findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToBluetoothFragment())
                    true
                }
                else->{
                    true
                }
            }
        }
    }

}