package com.victor.evotapplication.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.victor.evotapplication.fragments.AnnouncementsFragment
import com.victor.evotapplication.fragments.ChatFragment
import com.victor.evotapplication.fragments.VotesFragment

// Pager adapter for switching between Announcements and Votes fragments in a ViewPager2

class AssociationPagerAdapter(
    fragment: Fragment,
    private val associationId: String?
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> ChatFragment()
            1 -> AnnouncementsFragment()
            2 -> VotesFragment()
            else -> throw IllegalStateException("Invalid tab index")
        }

        fragment.arguments = Bundle().apply {
            putString("associationId", associationId)
        }

        return fragment
    }
}
