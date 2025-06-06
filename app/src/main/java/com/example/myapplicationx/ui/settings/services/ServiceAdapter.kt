package com.example.myapplicationx.ui.settings.services

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.database.model.ServiceEntity
import com.example.myapplicationx.databinding.ItemServiceBinding

class ServiceAdapter(
    private var services: List<ServiceEntity>,
    private val onEditClick: (ServiceEntity) -> Unit,
    private val onDeleteClick: (ServiceEntity) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(val binding: ItemServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(service: ServiceEntity) {
            binding.tvServiceName.text = service.serviceName
            // If serviceRate exists, use it; otherwise, use a dummy value.
            binding.tvServiceRate.text = if (service.isMetered) {
                service.unitPrice?.toString() ?: "0"
            } else {
                service.fixedPrice?.toString() ?: "0"
            }
            binding.btnEditService.setOnClickListener { onEditClick(service) }
            binding.btnDeleteService.setOnClickListener { onDeleteClick(service) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount(): Int = services.size

    /**
     * Updates the list of services using DiffUtil for efficient changes.
     */
    fun updateServices(newServices: List<ServiceEntity>) {
        val diffCallback = ServiceDiffCallback(services, newServices)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        services = newServices
        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * DiffUtil callback for calculating the differences between two lists of ServiceEntity.
     */
    class ServiceDiffCallback(
        private val oldList: List<ServiceEntity>,
        private val newList: List<ServiceEntity>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Assuming each ServiceEntity has a unique serviceId.
            return oldList[oldItemPosition].serviceId == newList[newItemPosition].serviceId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Compare the entire objects (requires proper equals implementation).
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}