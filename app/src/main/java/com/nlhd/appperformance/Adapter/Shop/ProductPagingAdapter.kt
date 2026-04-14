package com.nlhd.appperformance.Adapter.Shop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlhd.appperformance.databinding.ItemProductBinding

data class Product(
    val name: String,
    val image: String
)
class ProductPagingAdapter(
    private val list: List<Product>
): RecyclerView.Adapter<ProductPagingAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.binding.tvName.text = list[position].name
        Glide.with(holder.itemView.context).load(list[position].image).into(holder.binding.ivImage)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(val binding: ItemProductBinding): RecyclerView.ViewHolder(binding.root)
}