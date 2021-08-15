package com.example.foodist.domain.models

data class Venue(
  val id: String,
  val name: String,
  val location: Location,
)

typealias Venues = List<Venue>