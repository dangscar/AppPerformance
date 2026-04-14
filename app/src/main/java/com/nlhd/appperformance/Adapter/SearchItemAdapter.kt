package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.databinding.ItemSearchBinding

class SearchItemAdapter(
    private val list: List<String>
): RecyclerView.Adapter<SearchItemAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.binding.tvSearch.text = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class ViewHolder(val binding: ItemSearchBinding): RecyclerView.ViewHolder(binding.root)


}