package com.example.mapsdemo.main_screen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mapsdemo.R
import com.example.mapsdemo.databinding.FragmentWelcomeBinding
import com.example.mapsdemo.map_screen.MapsActivity
import com.example.mapsdemo.onboarding.FirstScreen
import com.example.mapsdemo.onboarding.SecondScreen
import com.example.mapsdemo.onboarding.ThirdScreen
import com.example.mapsdemo.onboarding.ViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_welcome.view.*


class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.mapBtn.setOnClickListener {
//            val intent = MapsActivity.newIntent(requireContext(), null)
//            startActivity(intent)
//        }
//
//        binding.settingsBtn.setOnClickListener {
////            it.findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToSettingsFragment())
//        }
//        binding.bluetoothBtn.setOnClickListener {
//            it.findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToBluetoothFragment())
//        }


        val bottomNavBar = view.findViewById<BottomNavigationView>(R.id.bottonnav)
        bottomNavBar?.selectedItemId = R.id.home
        bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    true
                }
                R.id.bluetooth ->{
                    view.findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToBluetoothFragment())
                    true
                }
                R.id.map_item ->{
                    val intent = MapsActivity.newIntent(requireContext(), null)
                    startActivity(intent)
                    true
                }
                R.id.settings ->{
                    view.findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToSettingsFragment())
                    true
                }
                else->{
                    true
                }
            }
        }

        val fragmentList = arrayListOf<Fragment>(
            FirstScreen(), SecondScreen(), ThirdScreen()
        )

        val adapter = ViewPagerAdapter(fragmentList, childFragmentManager, lifecycle)
        view.view_pager.adapter = adapter

    }
}