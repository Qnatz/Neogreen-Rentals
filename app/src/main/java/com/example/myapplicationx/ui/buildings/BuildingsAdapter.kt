package com.example.myapplicationx.ui.buildings

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplicationx.databinding.ItemBuildingBinding
import com.example.myapplicationx.database.model.BuildingEntity
import android.util.Log

class BuildingsAdapter(
    private var buildings: List<BuildingEntity>,
    private val onBuildingClick: (BuildingEntity) -> Unit
) : RecyclerView.Adapter<BuildingsAdapter.BuildingViewHolder>() {

    // Method to update the list of buildings
    fun submitList(newBuildings: List<BuildingEntity>) {
        buildings = newBuildings
        notifyDataSetChanged()
    }

    // Method to update a single building’s occupancy status
    fun updateBuildingOccupancy(buildingId: Int, occupiedUnits: Int, vacantUnits: Int) {
    val index = buildings.indexOfFirst { it.buildingId == buildingId }
    if (index != -1) {
        val updatedList = buildings.toMutableList().apply {
            this[index] = this[index].copy(occupiedUnits = occupiedUnits, vacantUnits = vacantUnits)
        }
        submitList(updatedList)
    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuildingViewHolder {
        val binding = ItemBuildingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuildingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuildingViewHolder, position: Int) {
        holder.bind(buildings[position])
    }

    override fun getItemCount(): Int = buildings.size

    inner class BuildingViewHolder(private val binding: ItemBuildingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(building: BuildingEntity) {
            binding.buildingName.text = building.buildingName
            binding.buildingLocation.text = building.buildingLocation
            binding.occupiedUnits.text = "Occupied: ${building.occupiedUnits}"
            binding.vacantUnits.text = "Vacant: ${building.vacantUnits}"

            Glide.with(binding.buildingImage.context)
                .load(Uri.parse(building.imageUrl))
                .into(binding.buildingImage)

            itemView.setOnClickListener { onBuildingClick(building) }
        }
    }
}