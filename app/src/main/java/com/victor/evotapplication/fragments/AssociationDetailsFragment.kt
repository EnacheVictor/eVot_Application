package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.victor.evotapplication.adapters.AssociationPagerAdapter
import com.victor.evotapplication.databinding.FragmentAssociationDetailsBinding

// Fragment that displays the details of an association with tabs for Announcements and Votes


class AssociationDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAssociationDetailsBinding
    private var associationId: String? = null
    private var associationName: String? = null
    private var associationLocation: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAssociationDetailsBinding.inflate(inflater, container, false)

        associationId = arguments?.getString("associationId")
        associationName = arguments?.getString("associationName")
        associationLocation = arguments?.getString("associationLocation")

        val pagerAdapter =
            AssociationPagerAdapter(this, associationId)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Chat"
                1 -> "Announcements"
                2 -> "Votes"
                else -> ""
            }
        }.attach()


        binding.associationTitle.text = associationName ?: "Association"
        binding.associationLoc.text = associationLocation ?: "AssociationLocation"

        return binding.root
    }
}
