package com.example.foodist.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.foodist.R
import com.example.foodist.ui.permission.PermissionRequest
import com.example.foodist.ui.permission.PermissionsFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


/**
 * The **VenueMapFragment** is responsible for displaying the map.
 * There are three primary operations that occur here:
 *  - The fragment will prompt the user for location permissions.
 *  - The map search area will be set using either the current location or the last viewed area
 *  - The map will populate with results as the user panes.
 */
class VenueMapFragment : PermissionsFragment() {

  private var mapLoaded: Boolean = false
  private lateinit var googleMap: GoogleMap

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    checkGrantedPermissions(PermissionRequest.LOCATION) {}
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
      setCurrentLocation()
    }
  }

  private fun setCurrentLocation() {
    val amsterdam = LatLng(52.36, 4.90)
    googleMap.addMarker(MarkerOptions().position(amsterdam).title("Marker in Amsterdam"))
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam, DEFAULT_ZOOM_LEVEL))
  }

  companion object {
    const val DEFAULT_ZOOM_LEVEL = 15.0f
  }
}