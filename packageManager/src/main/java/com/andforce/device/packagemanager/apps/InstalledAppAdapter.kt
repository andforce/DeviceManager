package com.andforce.device.packagemanager.apps

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.andforce.device.packagemanager.R

class InstalledAppAdapter(val context: Context): RecyclerView.Adapter<InstalledAppViewHolder>() {

    private val data: MutableList<AppBean> = mutableListOf()
    private var listener: OnUninstallClickListener? = null
    fun setOnUninstallClickListener(listener: OnUninstallClickListener) {
        this.listener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<AppBean>) {
        this.data.clear()
        this.data.addAll(data)
        this.data.sortBy { it.isSystem }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstalledAppViewHolder {
        val root = LayoutInflater.from(context).inflate(R.layout.installed_app_item_layout, parent, false)
        return InstalledAppViewHolder(root)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: InstalledAppViewHolder, position: Int) {
        val appBean = data[position]
        val appIcon = holder.itemView.findViewById<AppCompatImageView>(R.id.iv_app_icon)
        appIcon.setImageDrawable(appBean.icon)
        val appName = holder.itemView.findViewById<TextView>(R.id.tv_app_name)
        appName.text = appBean.appName
        appName.setTextColor(if (appBean.isSystem) Color.RED else Color.GREEN)

        val pkgName = holder.itemView.findViewById<TextView>(R.id.tv_app_package_name)
        pkgName.text = appBean.packageName
        val button = holder.itemView.findViewById<Button>(R.id.btn_uninstall)
        if (appBean.isSystem) {
            button.visibility = View.GONE
        } else {
            button.visibility = View.VISIBLE
        }
        button.setOnClickListener {
            listener?.onUninstallClick(appBean)
        }
    }
}

interface OnUninstallClickListener {
    fun onUninstallClick(appBean: AppBean)
}

class InstalledAppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

}