package com.example.mc_assignment_3_question_2

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class LoggingActivity : AppCompatActivity() {
    private lateinit var location: LocationData
    private var sampleCount = 0
    private val maxSamples = 100
    private lateinit var wifiManager: WifiManager
    private lateinit var adapter: ScanResultAdapter
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            processScanResults()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logging)

        val locationName = intent.getStringExtra("locationName")
        val locations = DataManager.loadLocations(this)
        location = locations.find { it.name == locationName }!!
        location.apData.clear()

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val recyclerView = findViewById<RecyclerView>(R.id.scanResultsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ScanResultAdapter(mutableListOf())
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.startLoggingButton).setOnClickListener {
            checkPermissionsAndStartLogging()
        }
    }

    private fun checkPermissionsAndStartLogging() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            startLogging()
        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLogging()
                } else {
                    Toast.makeText(
                        this,
                        "Location permission is required for WiFi scanning",
                        Toast.LENGTH_SHORT
                    ).show()
                    findViewById<Button>(R.id.startLoggingButton).isEnabled = true
                }
            }
        }
    }

    private fun startLogging() {
        findViewById<Button>(R.id.startLoggingButton).isEnabled = false
        sampleCount = 0
        registerReceiver(scanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        if (!wifiManager.startScan()) {
            Toast.makeText(this, "WiFi scan failed to start", Toast.LENGTH_SHORT).show()
            findViewById<Button>(R.id.startLoggingButton).isEnabled = true
        }
    }

    private fun processScanResults() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, stop logging
            stopLogging()
            return
        }

        val scanResults = wifiManager.scanResults
        for (result in scanResults) {
            val bssid = result.BSSID
            val ssid = result.SSID
            val level = result.level
            location.apData.getOrPut(bssid) { ApData(ssid, mutableListOf()) }.rssList.add(level)
        }
        sampleCount++
        updateUI(scanResults)
        if (sampleCount < maxSamples) {
            scheduleNextScan()
        } else {
            stopLogging()
        }
    }

    private fun scheduleNextScan() {
        Handler(Looper.getMainLooper()).postDelayed({
            wifiManager.startScan()
        }, 1000)
    }

    private fun stopLogging() {
        try {
            unregisterReceiver(scanReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
        DataManager.saveLocations(this, DataManager.loadLocations(this).apply {
            find { it.name == location.name }?.apData?.putAll(location.apData)
        })
        Toast.makeText(this, "Logging complete", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateUI(scanResults: List<android.net.wifi.ScanResult>) {
        findViewById<TextView>(R.id.sampleCountText).text = "Samples: $sampleCount/$maxSamples"
        adapter.updateResults(scanResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing && sampleCount > 0 && sampleCount < maxSamples) {
            try {
                unregisterReceiver(scanReceiver)
            } catch (e: IllegalArgumentException) {
                // Receiver not registered
            }
        }
    }
}

class ScanResultAdapter(private val scanResults: MutableList<android.net.wifi.ScanResult>) :
    RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text1: TextView = view.findViewById(android.R.id.text1)
        val text2: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = scanResults[position]
        holder.text1.text = "${result.SSID} (${result.BSSID})"
        holder.text2.text = "RSS: ${result.level} dBm"
    }

    override fun getItemCount() = scanResults.size

    fun updateResults(newResults: List<android.net.wifi.ScanResult>) {
        scanResults.clear()
        scanResults.addAll(newResults)
        notifyDataSetChanged()
    }
}