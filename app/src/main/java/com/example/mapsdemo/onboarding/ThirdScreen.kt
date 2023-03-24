package com.example.mapsdemo.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.mapsdemo.R
import com.example.mapsdemo.map_screen.MapsActivity
import kotlinx.android.synthetic.main.fragment_third_screen.view.*


class ThirdScreen : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third_screen, container, false)

        val viewPager =  activity?.findViewById<ViewPager2>(R.id.view_pager)

        view.next.setOnClickListener {
            val intent = MapsActivity.newIntent(requireContext(), null)
            startActivity(intent)
        }

        return view
    }

}