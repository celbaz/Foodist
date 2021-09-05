package com.example.foodist.ui.venueDetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.foodist.R
import com.example.foodist.domain.models.Location
import com.example.foodist.domain.models.Photo
import com.example.foodist.domain.models.VenueDetails
import com.example.foodist.utils.Status
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VenueDetailFragment : Fragment() {
  private lateinit var detailView: View
  private val args: VenueDetailFragmentArgs by navArgs()
  private val viewModel: VenueDetailViewModel by viewModels()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    detailView = inflater.inflate(R.layout.venue_detail_fragment, container, false)
    return detailView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    initEventHandlers()
    initObservers()
    viewModel.onViewReady(args.venueId)
  }

  private fun initEventHandlers() {
    detailView.findViewById<ImageView>(R.id.detailNavBack).setOnClickListener {
      val action = VenueDetailFragmentDirections.actionVenueDetailFragmentToVenueMapFragment()
      detailView.findNavController().navigate(action)
    }
  }

  private fun initObservers() {
    viewModel.cachedInformation.observe(viewLifecycleOwner) { venue ->
      updateTitle(venue.name)
    }

    viewModel.venueDetails.observe(viewLifecycleOwner) { venueDetails ->
      updateTitle(venueDetails.name)
      updateVenueDetails(venueDetails)
    }

    viewModel.requestStatus.observe(viewLifecycleOwner) { status ->
      when (status) {
        Status.LOADING -> toggleLoadingIndicator()
        Status.SUCCESS -> toggleLoadingIndicator()
        else -> {
          toggleLoadingIndicator(); displayErrorIndicator()
        }
      }
    }
  }

  private fun toggleLoadingIndicator() {
    val progressIndicator = detailView.findViewById<FrameLayout>(R.id.detailViewProgressIndicator)
    progressIndicator.visibility = if (progressIndicator.visibility == GONE) VISIBLE else GONE
  }

  private fun displayErrorIndicator() {
    detailView.findViewById<LinearLayout>(R.id.infoContainer).visibility = GONE
    detailView.findViewById<LinearLayout>(R.id.errorContainer).visibility = VISIBLE
  }

  private fun updateTitle(title: String) {
    detailView.findViewById<TextView>(R.id.businessDetailTitle).text = title
  }

  private fun updateVenueDetails(venueDetails: VenueDetails) {
    if (venueDetails.rating != null) {
      detailView.findViewById<TextView>(R.id.rating).text = venueDetails.rating.toString()
    }

    venueDetails.bestPhoto?.let { updateImage(it) }
    venueDetails.location?.let { updateLocationContainer(it) }

    detailView.findViewById<LinearLayout>(R.id.infoContainer).visibility = VISIBLE
  }

  private fun updateImage(photo: Photo) {
    val url = "${photo.prefix}original${photo.suffix}"
    val imageView = detailView.findViewById<ImageView>(R.id.venueImage)
    val transform = RoundedTransformationBuilder()
      .cornerRadiusDp(20f)
      .oval(false)
      .build()

    imageView.visibility = INVISIBLE
    Picasso.get().load(url).fit().transform(transform).into(imageView, object : com.squareup.picasso.Callback {
      override fun onSuccess() {
        imageView.visibility = VISIBLE
      }

      override fun onError(e: java.lang.Exception?) {}
    })
  }

  private fun updateLocationContainer(location: Location) {
    val locationContainer = detailView.findViewById<LinearLayout>(R.id.locationContainer)
    detailView.findViewById<TextView>(R.id.venueAddressLine1).text = location.address
    detailView.findViewById<TextView>(R.id.venueAddressLine2).text =
      listOfNotNull(location.city, location.country).joinToString(", ")

    locationContainer.setOnClickListener {
      val uriString = "geo:<${location.lat}>,<${location.lng}>?q=${location.lat},${location.lng}"
      val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
      startActivity(mapIntent)
    }

    locationContainer.visibility = VISIBLE
  }
}