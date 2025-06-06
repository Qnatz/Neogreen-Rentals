package com.example.myapplicationx.ui.buildings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.R
import com.example.myapplicationx.database.model.HouseEntity // Import the HouseEntity model

class HousesAdapter(
    private var houses: List<HouseEntity>, // List of HouseEntity
    private val onHouseClick: (Int) -> Unit // Click listener that receives houseId
) : RecyclerView.Adapter<HousesAdapter.HouseViewHolder>() {

    // ViewHolder class for individual house items
    class HouseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val houseName: TextView = view.findViewById(R.id.houseName) // Reference to house name TextView
        val houseStatus: TextView = view.findViewById(R.id.status) // Reference to status TextView
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HouseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_house, parent, false) // Inflate your item layout
        return HouseViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: HouseViewHolder, position: Int) {
        val house = houses[position]
        holder.houseName.text = house.houseName // Set the house name
        holder.houseStatus.text = house.status // Set status from the HouseEntity

        // Set click listener to pass the houseId
        holder.itemView.setOnClickListener {
            onHouseClick(house.houseId) // Pass house id when clicked
        }
    }

    // Return the size of the dataset (invoked by the layout manager)
    override fun getItemCount() = houses.size

    // Function to update houses data
    fun updateHouses(newHouses: List<HouseEntity>) {
        houses = newHouses
        notifyDataSetChanged() // Notify adapter to refresh the view
    }
}