package rajnishkmehta.sakshi.camera.vault

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            iconView.setImageDrawable(appInfo.icon)
            nameView.text = appInfo.name
            packageView.text = appInfo.packageName

            itemView.setOnClickListener {
                onClick(appInfo)
            }
        }
    }
}
