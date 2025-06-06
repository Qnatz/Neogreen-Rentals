package com.example.myapplicationx.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.databinding.ItemSettingBinding

class SettingsAdapter(
    private val settingList: List<Settings>,
    private val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    inner class SettingsViewHolder(private val binding: ItemSettingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(setting: Settings) {
            binding.settingName.text = setting.settingName
            binding.root.setOnClickListener { onItemClicked(setting.settingId) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val binding = ItemSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SettingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bind(settingList[position])
    }

    override fun getItemCount(): Int = settingList.size
}