package com.example.foodist.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.foodist.R
import com.example.foodist.ui.permission.PermissionRequest
import com.example.foodist.ui.permission.PermissionsFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint


/**
 * The **VenueMapFragment** is responsible for displaying the map.
 * There are three primary operations that occur here:
 *  - The fragment will prompt the user for location permissions.
 *  - The map search area will be set using either the current location or the last viewed area
 *  - The map will populate with results as the user panes.
 */
@AndroidEntryPoint
class VenueMapFragment : PermissionsFragment() {
  private var mapLoaded: Boolean = false
  private var cameraMovedByUser: Boolean = false
  private lateinit var googleMap: GoogleMap
  private val viewModel: VenueMapViewModel by viewModels()


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    checkGrantedPermissions(PermissionRequest.LOCATION) { viewModel.onMapReady(LatLng(40.7484, -73.9857), 250.0) }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.fragment_venue_map, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?

    mapFragment?.getMapAsync { gMap ->
      googleMap = gMap
      mapLoaded = true

      initMapEventHandlers()
      initObservers()
    }
  }

  private fun setMapArea(currentPosition: LatLng, title: String) {
    val zoomLevel = viewModel.zoom.value ?: 15.0f
    googleMap.addMarker(MarkerOptions().position(currentPosition).title(title))
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, zoomLevel))
  }

  private fun initMapEventHandlers() {
    googleMap.setOnCameraMoveStartedListener {
      cameraMovedByUser = (it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE)
    }

    googleMap.setOnCameraIdleListener {
      if (cameraMovedByUser) {
        val cameraPosition = googleMap.cameraPosition
        viewModel.onMapMovement(cameraPosition.target, cameraPosition.zoom)
        cameraMovedByUser = false
      }
    }
  }

  private fun initObservers() {
    viewModel.venues.observe(viewLifecycleOwner, Observer { places ->
      if (mapLoaded) {
        places.forEach { places ->
          setMapArea(LatLng(places.location.lat, places.location.lng), places.name)
        }
      }
    })
  }

}