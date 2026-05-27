package com.nlhd.appperformance.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.nlhd.appperformance.R

class LoadingAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<LoadingAdapter.LoadingViewHolder>() {

    inner class LoadingViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val progressBar: LottieAnimationView = view.findViewById(R.id.loadingView)
        val btnRetry: Button = view.findViewById(R.id.errorView)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_load_state, parent, false)
        return LoadingViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LoadingViewHolder,
        loadState: LoadState
    ) {

        //holder.progressBar.visibility = if (loadState is LoadState.Loading)  View.VISIBLE else View.GONE
        holder.btnRetry.visibility = if (loadState is LoadState.Error)  View.VISIBLE else View.GONE
        holder.btnRetry.setOnClickListener {
            retry()
        }
    }
}
