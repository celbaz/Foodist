package com.example.foodist.domain.models

data class Location(
  val address: String?,
  val lat: Double,
  val lng: Double,
  val postalCode: String?,
  val city: String?,
  val state: String?,
  val country: String?,
  val formattedAddress: List<String>?,
)