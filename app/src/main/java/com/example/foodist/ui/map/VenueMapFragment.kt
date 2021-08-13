package com.example.foodist.ui.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.foodist.R


/**
 * The **VenueMapFragment** is responsible for displaying a map with points of interest nearby.
 */
class VenueMapFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_venue_map, container, false)
  }
}