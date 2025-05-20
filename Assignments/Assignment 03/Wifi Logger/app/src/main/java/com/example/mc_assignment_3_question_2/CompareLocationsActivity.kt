package com.example.mc_assignment_3_question_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CompareLocationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_locations)

        val locations = DataManager.loadLocations(this)
        val allBssids = locations.flatMap { it.apData.keys }.toSet()

        val recyclerView = findViewById<RecyclerView>(R.id.compareRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = CompareAdapter(allBssids, locations)
        recyclerView.adapter = adapter
    }
}

class CompareAdapter(private val bssids: Set<String>, private val locations: List<LocationData>) :
    RecyclerView.Adapter<CompareAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.compare_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bssid = bssids.toList()[position]
        val apData = locations.mapNotNull { it.apData[bssid] }.firstOrNull()
        val ssid = apData?.ssid ?: "Unknown"
        holder.itemView.findViewById<TextView>(R.id.apInfoText).text = "$ssid ($bssid)"

        val rangesText = locations.filter { it.apData.containsKey(bssid) }
            .joinToString("\n") { loc ->
                val rssList = loc.apData[bssid]!!.rssList
                val minRss = rssList.minOrNull() ?: 0
                val maxRss = rssList.maxOrNull() ?: 0
                "${loc.name}: $minRss to $maxRss dBm"
            }
        holder.itemView.findViewById<TextView>(R.id.rangesText).text = rangesText
    }

    override fun getItemCount() = bssids.size
}