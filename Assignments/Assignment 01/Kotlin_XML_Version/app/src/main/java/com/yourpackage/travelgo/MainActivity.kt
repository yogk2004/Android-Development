package com.yourpackage.travelgo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yourpackage.travelgo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use binding to access views
        binding.nextButton.setOnClickListener {
            val iNext = Intent(this@MainActivity, IntroRouteJourney::class.java)
            startActivity(iNext)
        }
    }
}