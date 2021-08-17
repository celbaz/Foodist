package com.example.foodist.domain.models

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

data class Photo(
  val id: String,
  val prefix: String,
  val suffix: String,
  val width: Int,
  val height: Int,
)

data class VenueDetails(
  val name: String,
  val contact: Contact?,
  val location: Location?,
  val url: String?,
  val rating: Double?,
  val stats: Stats?,
  val bestPhoto: Photo?,
)

