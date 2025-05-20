package com.example.mc_assignment_3_question_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ViewLocationDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_location_data)

        val locationName = intent.getStringExtra("locationName")
        val locations = DataManager.loadLocations(this)
        val location = locations.find { it.name == locationName }!!

        findViewById<TextView>(R.id.locationNameText).text = location.name
        val recyclerView = findViewById<RecyclerView>(R.id.apDataRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ApDataAdapter(location.apData)
        recyclerView.adapter = adapter
    }
}

class ApDataAdapter(private val apData: Map<String, ApData>) :
    RecyclerView.Adapter<ApDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bssid = apData.keys.toList()[position]
        val data = apData[bssid]!!
        val minRss = data.rssList.minOrNull() ?: 0
        val maxRss = data.rssList.maxOrNull() ?: 0
        holder.itemView.findViewById<TextView>(android.R.id.text1).text = "${data.ssid} ($bssid)"
        holder.itemView.findViewById<TextView>(android.R.id.text2).text = "RSS Range: $minRss to $maxRss dBm"
    }

    override fun getItemCount() = apData.size
}