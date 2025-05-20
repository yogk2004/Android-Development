package com.yourpackage.travelgo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yourpackage.travelgo.databinding.ItemStopBinding

class StopsAdapter(private val stops: List<Stop>) : RecyclerView.Adapter<StopsAdapter.StopViewHolder>() {

    private var isKm = true
    private var currentStopIndex = 0 // Track the current stop index

    class StopViewHolder(val binding: ItemStopBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopViewHolder {
        val binding = ItemStopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StopViewHolder, position: Int) {
        val stop = stops[position]
        with(holder.binding) {
            tvSource.text = stop.source
            tvDestination.text = stop.destination
            tvVisa.text = stop.visaRequirement
            tvDistance.text = if (isKm) "${stop.distanceKm} km" else "${stop.distanceMi} miles"
            tvFlightTime.text = "${stop.flightTimeHrs} hrs"

            // Change background color for the current stop
            if (position == currentStopIndex) {
                root.setBackgroundColor(Color.parseColor("#1e90ff")) // Highlight color
            } else {
                root.setBackgroundColor(Color.parseColor("#1e1e1e")) // Default color
            }
        }
    }

    override fun getItemCount(): Int = stops.size

    fun switchUnit(isKm: Boolean) {
        this.isKm = isKm
        notifyDataSetChanged()
    }

    fun resetStops() {
        currentStopIndex = -1  // No highlight
        notifyDataSetChanged()
    }

    fun moveToNextStop() {
        if (currentStopIndex < stops.size - 1) {
            currentStopIndex++
            notifyDataSetChanged()
        }
    }
    fun getCurrentStopIndex(): Int = currentStopIndex
}