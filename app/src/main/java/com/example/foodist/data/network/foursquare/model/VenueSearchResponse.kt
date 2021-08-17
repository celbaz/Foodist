package com.example.foodist.data.network.foursquare.model

import com.squareup.moshi.Json

data class Icon(
  val prefix: String,
  val suffix: String,
)

data class LocationResponse(
  val address: String?,
  val crossStreet: String?,
  val lat: Double,
  val lng: Double,
  val distance: Double?,
  val postalCode: String?,
  val city: String?,
  val state: String?,
  @Json(name = "cc") val country: String?,
  val formattedAddress: List<String>?,
)

data class VenueResponse(
  val id: String,
  val name: String,
  val location: LocationResponse,
  val categories: List<CategoryResponse>?,
)

data class CategoryResponse(
  val id: String,
  val name: String,
  val pluralName: String,
  val shortName: String,
  val icon: Icon,
  val primary: Boolean,
)

data class VenueSearchResults(
  val venues: List<VenueResponse>,
)

data class VenueSearchResponse(
  val response: VenueSearchResults,
)