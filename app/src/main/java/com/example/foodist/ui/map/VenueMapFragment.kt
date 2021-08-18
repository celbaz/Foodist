package com.example.foodist.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.foodist.R
import com.example.foodist.domain.models.Venue
import com.example.foodist.services.PermissionsService
import com.example.foodist.utils.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.progressindicator.LinearProgressIndicator
import dagger.hilt.android.AndroidEntryPoint


/**
 * The **VenueMapFragment** is responsible for displaying the map.
 * There are three primary operations that occur here:
 *  - The fragment will prompt the user for location permissions.
 *  - The map search area will be set using either the current location or the last viewed area
 *  - The map will populate with results as the user panes.
 */
@AndroidEntryPoint
class VenueMapFragment : Fragment() {
  private var mapLoaded: Boolean = false
  private var cameraMovedByUser: Boolean = false
  private lateinit var googleMap: GoogleMap
  private lateinit var progressIndicator: LinearProgressIndicator
  private lateinit var venueCardView: RelativeLayout
  private val viewModel: VenueMapViewModel by viewModels()
  private val markerMap: MutableMap<String, Marker> = mutableMapOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewModel.onMapReady(PermissionsService(this))
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
    progressIndicator = view.findViewById(R.id.mapProgressIndicator)
    venueCardView = view.findViewById(R.id.venueCardView)

    mapFragment?.getMapAsync { gMap ->
      googleMap = gMap
      mapLoaded = true

      initMapEventHandlers()
      initObservers()
    }
  }

  private fun setMapPin(currentPosition: LatLng, id: String) {
    if (markerMap[id] != null) return

    var marker = googleMap.addMarker(
      MarkerOptions()
        .position(currentPosition)
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
    )

    marker.tag = id
    markerMap[id] = marker
  }

  private fun setMapArea(currentPosition: LatLng) {
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, viewModel.zoomValue))
  }

  private fun setCardView(id: String): Boolean {
    var venue: Venue = viewModel.venues.value?.find { place -> place.id == id } ?: return true

    venueCardView.setOnClickListener {
      val action = VenueMapFragmentDirections.actionVenueMapFragmentToVenueDetailFragment(id)
      venueCardView.findNavController().navigate(action)
    }

    val address: String? = venue.location.formattedAddress?.first() ?: venue.location.address
    venueCardView.findViewById<TextView>(R.id.businessTitle).text = venue.name
    if (address != null) {
      venueCardView.findViewById<TextView>(R.id.shortAddress).text = address
    }

    if (venueCardView.visibility == INVISIBLE) {
      venueCardView.visibility = VISIBLE
    }

    return true
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

    googleMap.setOnMarkerClickListener { marker -> setCardView(marker.tag as String) }
    googleMap.setOnMapClickListener { venueCardView.visibility = INVISIBLE }
  }

  private fun initObservers() {
    viewModel.venueRequestStatus.observe(viewLifecycleOwner) { status ->
      progressIndicator.visibility = if (status == Status.LOADING) VISIBLE else INVISIBLE
    }

    viewModel.venues.observe(viewLifecycleOwner) { locations ->
      if (mapLoaded && locations.isNotEmpty()) {
        locations.forEach { location ->
          setMapPin(LatLng(location.location.lat, location.location.lng), location.id)
        }
      }
    }

    viewModel.setMapArea.observe(viewLifecycleOwner) { shouldSet ->
      if (shouldSet) setMapArea(viewModel.currentCenterPoint.value!!)
    }
  }
}