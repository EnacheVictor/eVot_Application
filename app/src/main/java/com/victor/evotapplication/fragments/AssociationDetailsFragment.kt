package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.victor.evotapplication.databinding.FragmentAssociationDetailsBinding

class AssociationDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAssociationDetailsBinding
    private var associationId: String? = null
    private var associationName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssociationDetailsBinding.inflate(inflater, container, false)

        arguments?.let {
            associationId = it.getString("associationId")
            associationName = it.getString("associationName")
        }

        binding.associationTitle.text = associationName ?: "Asociație"

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> Fragment()
                    1 -> Fragment()
                    else -> throw IllegalStateException("Unexpected position $position")
                }
            }
        }
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Votes"
                1 -> "Announcements"
                else -> "Tab $position"
            }
        }.attach()

        return binding.root
    }
}