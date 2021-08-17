package com.example.foodist.ui.venueDetail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.foodist.R
import com.example.foodist.domain.models.VenueDetails
import com.example.foodist.utils.Status
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VenueDetailFragment : Fragment() {
  private lateinit var detailView: View
  private lateinit var viewModel: VenueDetailViewModel
  private val args: VenueDetailFragmentArgs by navArgs()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    detailView = inflater.inflate(R.layout.venue_detail_fragment, container, false)
    return detailView
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    viewModel = ViewModelProvider(this).get(VenueDetailViewModel::class.java)

    setEventHandler()
    initObservers()
    viewModel.onViewReady(args.venueId)
  }

  private fun setEventHandler() {
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
        Status.ERROR_NETWORK -> displayErrorIndicator()
      }
    }
  }

  private fun updateTitle(title: String) {
    detailView.findViewById<TextView>(R.id.businessDetailTitle).text = title
  }

  private fun updateVenueDetails(venueDetails: VenueDetails) {
    venueDetails.rating.let {
      detailView.findViewById<TextView>(R.id.rating).text = venueDetails.rating.toString()
    }

    venueDetails.bestPhoto?.let {
      val url = "${venueDetails.bestPhoto.prefix}300x500${venueDetails.bestPhoto.suffix}"
      val imageView = detailView.findViewById<ImageView>(R.id.venueImage)
      val transform = RoundedTransformationBuilder()
        .cornerRadiusDp(20f)
        .oval(false)
        .build()

      Picasso.get().load(url).fit().transform(transform).into(imageView, object : com.squareup.picasso.Callback {
        override fun onSuccess() {}
        override fun onError(e: java.lang.Exception?) {
          imageView.setImageResource(R.drawable.skeleton)
        }
      })
    }

    val locationContainer = detailView.findViewById<LinearLayout>(R.id.locationContainer)
    if (venueDetails.location == null) {
      locationContainer.visibility = INVISIBLE
    } else {
      detailView.findViewById<TextView>(R.id.venueAddressLine1).text = venueDetails.location.address
      detailView.findViewById<TextView>(R.id.venueAddressLine2).text =
        "${venueDetails.location.city}, ${venueDetails.location.country}"
      locationContainer.visibility = VISIBLE
      locationContainer.setOnClickListener {
        val gmmIntentUri = Uri.parse("geo:${venueDetails.location.lat},${venueDetails.location.lng}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        startActivity(mapIntent)
      }
    }

    venueDetails.location.let {

    }
    venueDetails.stats?.tipCount?.let {}

  }

  private fun toggleLoadingIndicator() {
    val progressIndicator = detailView.findViewById<CircularProgressIndicator>(R.id.detailViewProgressIndicator)
    progressIndicator.visibility = if (progressIndicator.visibility == INVISIBLE) VISIBLE else INVISIBLE
  }

  private fun displayErrorIndicator() {
    toggleLoadingIndicator()
  }
}