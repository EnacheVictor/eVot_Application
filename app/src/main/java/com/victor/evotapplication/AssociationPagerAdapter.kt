package com.victor.evotapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.victor.evotapplication.fragments.AnnouncementsFragment
import com.victor.evotapplication.fragments.VotesFragment

class AssociationPagerAdapter(
    fragment: Fragment,
    private val associationId: String?
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> AnnouncementsFragment()
            1 -> VotesFragment()
            else -> throw IllegalStateException("Invalid tab index")
        }

        fragment.arguments = Bundle().apply {
            putString("associationId", associationId)
        }

        return fragment
    }
}
