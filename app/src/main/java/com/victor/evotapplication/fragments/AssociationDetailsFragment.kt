package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.victor.evotapplication.adapters.AssociationPagerAdapter
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

        associationId = arguments?.getString("associationId")
        associationName = arguments?.getString("associationName")

        val pagerAdapter =
            AssociationPagerAdapter(this, associationId)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Announcements" else "Votes"
        }.attach()

        binding.associationTitle.text = associationName ?: "Association"

        return binding.root
    }
}
