package com.victor.evotapplication.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.victor.evotapplication.R
import com.victor.evotapplication.databinding.FragmentAdminPanelBinding

class AdminPanelFragment : Fragment() {

    private lateinit var binding: FragmentAdminPanelBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false)

        binding.addResidentBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddResidentFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.addInvoiceBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddInvoiceFragment())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }
}
