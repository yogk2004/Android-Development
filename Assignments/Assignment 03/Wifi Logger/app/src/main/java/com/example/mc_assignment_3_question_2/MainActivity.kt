package com.example.mc_assignment_3_question_2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var locations: MutableList<LocationData>
    private lateinit var adapter: LocationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locations = DataManager.loadLocations(this)
        val recyclerView = findViewById<RecyclerView>(R.id.locationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LocationsAdapter(locations, this)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.addLocationButton).setOnClickListener {
            showAddLocationDialog()
        }

        findViewById<Button>(R.id.compareButton).setOnClickListener {
            if (locations.count { it.apData.isNotEmpty() } >= 3) {
                startActivity(Intent(this, CompareLocationsActivity::class.java))
            } else {
                Toast.makeText(this, "Need at least three locations with data", Toast.LENGTH_SHORT).show()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        locations = DataManager.loadLocations(this)
        adapter.updateLocations(locations)
    }

    private fun showAddLocationDialog() {
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Add Location")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty() && locations.none { it.name == name }) {
                    locations.add(LocationData(name, mutableMapOf()))
                    adapter.notifyDataSetChanged()
                    DataManager.saveLocations(this, locations)
                } else {
                    Toast.makeText(this, "Invalid or duplicate name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class LocationsAdapter(private var locations: List<LocationData>, private val context: MainActivity) :
    RecyclerView.Adapter<LocationsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locations[position]
        holder.itemView.findViewById<TextView>(android.R.id.text1).text = location.name
        holder.itemView.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(location.name)
                .setItems(arrayOf("Log Data", "View Data", "Rename")) { _, which ->
                    when (which) {
                        0 -> {
                            val intent = Intent(context, LoggingActivity::class.java)
                            intent.putExtra("locationName", location.name)
                            context.startActivity(intent)
                        }
                        1 -> {
                            val intent = Intent(context, ViewLocationDataActivity::class.java)
                            intent.putExtra("locationName", location.name)
                            context.startActivity(intent)
                        }
                        2 -> {
                            showRenameDialog(location)
                        }
                    }
                }
                .show()
        }
    }

    override fun getItemCount() = locations.size

    fun updateLocations(newLocations: List<LocationData>) {
        locations = newLocations
        notifyDataSetChanged()
    }

    private fun showRenameDialog(location: LocationData) {
        val editText = EditText(context)
        editText.setText(location.name)
        AlertDialog.Builder(context)
            .setTitle("Rename Location")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty() && locations.none { it.name == newName && it != location }) {
                    location.name = newName
                    notifyDataSetChanged()
                    DataManager.saveLocations(context, locations)
                } else {
                    Toast.makeText(context, "Invalid or duplicate name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}