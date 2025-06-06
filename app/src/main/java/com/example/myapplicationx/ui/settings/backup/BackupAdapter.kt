package com.example.myapplicationx.ui.settings.backup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.R  // Ensure this points to the correct package

class BackupAdapter(
    private var backupList: List<String>, 
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<BackupAdapter.BackupViewHolder>() {

    inner class BackupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val backupName: TextView = itemView.findViewById(R.id.backup_name)
        val deleteButton: Button = itemView.findViewById(R.id.button_delete)

        fun bind(backup: String) {
            backupName.text = backup
            deleteButton.setOnClickListener { onDelete(backup) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_backup, parent, false)
        return BackupViewHolder(view)
    }

    override fun onBindViewHolder(holder: BackupViewHolder, position: Int) {
        holder.bind(backupList[position])
    }

    override fun getItemCount(): Int = backupList.size

    fun updateBackups(newBackups: List<String>) {
        backupList = newBackups
        notifyDataSetChanged()
    }
}