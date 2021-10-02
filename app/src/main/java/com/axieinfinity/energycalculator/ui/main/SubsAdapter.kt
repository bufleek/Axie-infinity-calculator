package com.axieinfinity.energycalculator.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.axieinfinity.energycalculator.R

class SubsAdapter(
    private val onSubClicked: () -> Unit
) : RecyclerView.Adapter<SubsAdapter.ViewHolder>() {
    private val subs: ArrayList<SkuDetails> = ArrayList()
    private var isLoading = true

    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.tv_sub_name)
        private val desc = itemView.findViewById<TextView>(R.id.tv_sub_desc)
        private val btnSub = itemView.findViewById<Button>(R.id.subscribe)

        init {
            btnSub.setOnClickListener {
                onSubClicked()
            }
        }

        fun bind(sku: SkuDetails) {
            title.text = sku.title
            desc.text = sku.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!isLoading) {
            holder.bind(subs[position])
        }
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun updateData(newSubs: ArrayList<SkuDetails>, isLoading: Boolean = false) {
        subs.clear()
        subs += newSubs
        this.isLoading = isLoading
        notifyDataSetChanged()
    }
}