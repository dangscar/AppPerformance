package com.nlhd.appperformance.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.UIModel.HeaderUiModel
import com.nlhd.appperformance.R

class HeaderProfileAdapter(
): RecyclerView.Adapter<HeaderProfileAdapter.HeaderVH>() {

    companion object{
        const val VIEW_TYPE = 1000
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HeaderVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_profile, parent, false)
        return HeaderVH(view)
    }

    override fun onBindViewHolder(
        holder: HeaderVH,
        position: Int
    ) {

    }

    override fun getItemCount(): Int {
        return 1
    }

    inner class HeaderVH(view: View): RecyclerView.ViewHolder(view) {

    }
}