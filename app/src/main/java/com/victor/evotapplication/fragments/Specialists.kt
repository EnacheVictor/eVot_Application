package com.victor.evotapplication.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.victor.evotapplication.R
import com.victor.evotapplication.adapters.SpecialistAdapter
import com.victor.evotapplication.models.Specialist

class Specialists : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var specialistAdapter: SpecialistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_specialists, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSpecialists)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val specialistsList = listOf(
            Specialist(
                name = "Electrician",
                description = "Reparații electrice, montare prize, siguranțe și corpuri de iluminat.",
                imageUrl = "https://link_catre_imagine_electrician.jpg",
                contactInfo = "0722 123 456"
            ),
            Specialist(
                name = "Instalator",
                description = "Reparații țevi, montaj chiuvete, robineți, mașini de spălat.",
                imageUrl = "https://link_catre_imagine_instalator.jpg",
                contactInfo = "0744 987 654"
            ),
            Specialist(
                name = "Zugrav",
                description = "Zugrăveli interioare, reparații pereți, glet și vopsele lavabile.",
                imageUrl = "https://link_catre_imagine_zugrav.jpg",
                contactInfo = "0733 456 789"
            )
            // ➔ Poți adăuga câți specialiști vrei aici!
        )

        specialistAdapter = SpecialistAdapter(specialistsList)
        recyclerView.adapter = specialistAdapter

        return view
    }
}