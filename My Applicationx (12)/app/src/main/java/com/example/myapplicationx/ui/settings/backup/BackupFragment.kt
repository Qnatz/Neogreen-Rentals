package com.example.myapplicationx.ui.settings.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationx.R
import com.example.myapplicationx.ui.settings.backup.BackupViewModel
import com.example.myapplicationx.ui.settings.backup.BackupAdapter



class BackupFragment : Fragment() {

    private lateinit var restoreLayout: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonBackup: TextView
    private lateinit var buttonRestore: TextView
    private lateinit var arrowIcon: ImageView
    private val backupViewModel: BackupViewModel by viewModels()
    private lateinit var adapter: BackupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_backup, container, false)

        restoreLayout = view.findViewById(R.id.restore_layout)
        recyclerView = view.findViewById(R.id.recycler_view_backups)
        buttonBackup = view.findViewById(R.id.text_backup_now)
        buttonRestore = view.findViewById(R.id.text_restore_database)
        arrowIcon = view.findViewById(R.id.arrow_icon)

        adapter = BackupAdapter(emptyList()) { backupName ->
            backupViewModel.deleteBackup(backupName) // Delete backup on click
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        backupViewModel.availableBackups.observe(viewLifecycleOwner, Observer { backups ->
            adapter.updateBackups(backups)
        })

        buttonBackup.setOnClickListener {
            backupViewModel.performBackup() // Trigger backup
        }

        buttonRestore.setOnClickListener {
            toggleRestoreLayout()
        }

        return view
    }

    private fun toggleRestoreLayout() {
        if (restoreLayout.visibility == View.GONE) {
            restoreLayout.visibility = View.VISIBLE
            arrowIcon.setImageResource(R.drawable.ic_arrow_up) // change to up arrow
            backupViewModel.loadAvailableBackups() // Load backups
        } else {
            restoreLayout.visibility = View.GONE
            arrowIcon.setImageResource(R.drawable.ic_arrow_down) // change to down arrow
        }
    }
}