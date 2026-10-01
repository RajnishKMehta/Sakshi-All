/*
 * Copyright 2026 Rajnish Kumar
 * SPDX-License-Identifier: Apache-2.0
 */
package rajnishkmehta.sakshi.camera.vault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rajnishkmehta.sakshi.camera.R

class VaultAppAdapter(
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<VaultAppAdapter.ViewHolder>() {

    private var apps: List<AppInfo> = emptyList()

    fun submitList(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vault_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.app_icon)
        private val nameView: TextView = itemView.findViewById(R.id.app_name)
        private val packageView: TextView = itemView.findViewById(R.id.app_package)

        fun bind(appInfo: AppInfo) {
            nameView.text = appInfo.name
            packageView.text = appInfo.packageName

            iconView.setImageResource(android.R.drawable.sym_def_app_icon)

            CoroutineScope(Dispatchers.Main).launch {
                val icon = withContext(Dispatchers.IO) {
                    try {
                        itemView.context.packageManager.getApplicationIcon(appInfo.packageName)
                    } catch (e: Exception) {
                        null
                    }
                }
                if (icon != null) {
                    iconView.setImageDrawable(icon)
                }
            }

            itemView.setOnClickListener {
                onClick(appInfo)
            }
        }
    }
}
