package com.example.foodist.data.network.foursquare.mappers

import com.example.foodist.data.network.foursquare.model.VenueSearchResponse
import com.example.foodist.domain.models.Location
import com.example.foodist.domain.models.Venue
import com.example.foodist.domain.models.Venues
import retrofit2.Response

class VenueMapper {
  fun mapVenueResponseToDomain(payload: Response<VenueSearchResponse>): Venues {
    return payload.body()?.response?.venues?.map {
      Venue(
        it.id,
        it.name,
        Location(
          it.location.address,
          it.location.lat,
          it.location.lng,
          it.location.postalCode,
          it.location.city,
          it.location.state,
          it.location.country,
          it.location.formattedAddress
        )
      )
    } ?: listOf()
  }
}