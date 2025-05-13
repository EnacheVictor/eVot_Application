package com.victor.evotapplication.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.victor.evotapplication.AddListingActivity
import com.victor.evotapplication.R
import com.victor.evotapplication.adapters.ListingAdapter
import com.victor.evotapplication.models.Listing
import com.victor.evotapplication.models.ListingType

class RentSell : Fragment() {
    private lateinit var spinnerAssociation: Spinner
    private lateinit var textViewAdvancedFilter: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddListing: FloatingActionButton

    private lateinit var listingAdapter: ListingAdapter
    private var listings: ArrayList<Listing> = arrayListOf()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var associationList: List<Association> = listOf()
    private var selectedAssociationId: String? = null

    private var filterType: ListingType? = null
    private var lastVisibleSnapshot: DocumentSnapshot? = null
    private var isLoading = false
    private val pageSize = 4

    data class Association(val id: String, val name: String)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rent_sell, container, false)

        spinnerAssociation = view.findViewById(R.id.spinnerAssociation)
        textViewAdvancedFilter = view.findViewById(R.id.textViewAdvancedFilter)
        recyclerView = view.findViewById(R.id.recyclerViewListings)
        fabAddListing = view.findViewById(R.id.fabAddListing)

        listingAdapter = ListingAdapter(listings)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = listingAdapter

        loadUserAssociations()

        spinnerAssociation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedAssociationId = associationList[position].id
                listings.clear()
                listingAdapter.notifyDataSetChanged()
                lastVisibleSnapshot = null
                loadListingsPage()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!isLoading && lastVisible >= totalItemCount - 2) {
                    loadListingsPage()
                }
            }
        })

        textViewAdvancedFilter.setOnClickListener {
            showAdvancedFilterDialog()
        }

        fabAddListing.setOnClickListener {
            val intent = Intent(requireContext(), AddListingActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun loadUserAssociations() {
        val currentUserId = auth.currentUser?.uid ?: return

        firestore.collection("user-type").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                val associationsIds = document["associations"] as? List<String> ?: listOf()

                if (associationsIds.isEmpty()) {
                    Toast.makeText(context, "Nu faci parte din nicio asociație!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                firestore.collection("associations")
                    .whereIn(FieldPath.documentId(), associationsIds)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        associationList = snapshot.documents.map {
                            Association(id = it.id, name = it.getString("name") ?: "Fără nume")
                        }

                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.spinner_item_black,
                            associationList.map { it.name }
                        )

                        adapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
                        spinnerAssociation.adapter = adapter

                        if (associationList.isNotEmpty()) {
                            selectedAssociationId = associationList[0].id
                            listings.clear()
                            listingAdapter.notifyDataSetChanged()
                            lastVisibleSnapshot = null
                            loadListingsPage()
                        }
                    }
            }
    }

    private fun showAdvancedFilterDialog() {
        val options = arrayOf("Toate", "De închiriat", "De vânzare")

        AlertDialog.Builder(requireContext())
            .setTitle("Selectează filtru")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> filterType = null
                    1 -> filterType = ListingType.RENT
                    2 -> filterType = ListingType.SELL
                }
                listings.clear()
                listingAdapter.notifyDataSetChanged()
                lastVisibleSnapshot = null
                loadListingsPage()
            }
            .show()
    }

    private fun loadListingsPage() {
        if (selectedAssociationId == null || isLoading) return

        isLoading = true
        var query = firestore.collection("listings")
            .whereEqualTo("associationId", selectedAssociationId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize.toLong())

        lastVisibleSnapshot?.let {
            query = query.startAfter(it)
        }

        query.get()
            .addOnSuccessListener { snapshot ->
                val newListings = snapshot.documents.mapNotNull { it.toObject(Listing::class.java) }
                    .filter { filterType == null || it.type == filterType }

                listings.addAll(newListings)
                listingAdapter.notifyDataSetChanged()

                if (snapshot.documents.isNotEmpty()) {
                    lastVisibleSnapshot = snapshot.documents.last()
                }

                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(requireContext(), "Eroare la încărcarea anunțurilor.", Toast.LENGTH_SHORT).show()
            }
    }
}
