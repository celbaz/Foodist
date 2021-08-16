package com.example.foodist.data.network.geolocation.model

data class GeolocationRequest(
  val radioType: String,
  val considerIp: Boolean = true,
)