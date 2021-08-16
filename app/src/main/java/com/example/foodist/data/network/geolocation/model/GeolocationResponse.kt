package com.example.foodist.data.network.geolocation.model

data class GeoLocation(
  val lat: Double,
  val lng: Double,
)

data class GeolocationResponse(
  val location: GeoLocation,
  val accuracy: Int,
)