package com.example.foodist.data.network.foursquare.model

data class Contact(
  val phone: String?,
  val facebook: String?,
)

data class Stats(
  val tipCount: Int?,
  val usersCount: Int?,
  val checkinsCount: Int?,
  val visitsCount: Int?,
)

data class PhotoResponse(
  val id: String,
  val prefix: String,
  val suffix: String,
  val width: Int,
  val height: Int,
)

data class VenueDetail(
  val name: String,
  val contact: Contact?,
  val location: LocationResponse,
  val canonicalUrl: String?,
  val categories: List<CategoryResponse>?,
  val rating: Double?,
  val stats: Stats?,
  val bestPhoto: PhotoResponse?,
)

data class VenueDetailResult(
  val venue: VenueDetail,
)

data class VenueDetailsResponse(
  val response: VenueDetailResult
)