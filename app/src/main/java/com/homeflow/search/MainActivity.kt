package com.homeflow.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.elevation.SurfaceColors
import com.homeflow.search.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    setSupportActionBar(binding.toolbar)
    window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
    title = "Luios david"

    val topBar = binding.toolbar
    val searchView = binding.mainSearchView

    topBar.setNavigationOnClickListener {
      searchView.openSearch()
    }
  }
}