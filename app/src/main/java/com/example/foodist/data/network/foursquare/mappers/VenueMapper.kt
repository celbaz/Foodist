package com.example.foodist.data.network.foursquare.mappers

import com.example.foodist.data.network.foursquare.model.*
import com.example.foodist.domain.models.*
import com.example.foodist.domain.models.Contact
import com.example.foodist.domain.models.Stats
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
//mapVenueDetailsResponseToDomain
  fun mapVenueDetailsResponseToDomain(payload: Response<VenueDetailsResponse>): VenueDetails {
    val resp = payload.body()?.response?.venue
    return VenueDetails(
      resp!!.name,
      Contact(
        resp?.contact?.phone,
        resp?.contact?.facebook,
      ),
      Location(
        resp?.location?.address,
        resp?.location?.lat  ?: 0.0,
        resp?.location?.lng ?: 0.0,
        resp?.location?.postalCode,
        resp?.location?.city,
        resp?.location?.state,
        resp?.location?.country,
        resp?.location?.formattedAddress
      ),
      resp?.canonicalUrl,
      resp?.rating,
      Stats(
        resp?.stats?.tipCount,
        resp?.stats?.usersCount,
        resp?.stats?.tipCount,
        resp?.stats?.visitsCount,
      ),
      if (resp?.bestPhoto != null) Photo(
        resp.bestPhoto?.id,
        resp.bestPhoto?.prefix,
        resp.bestPhoto?.suffix,
        resp.bestPhoto?.width,
        resp.bestPhoto?.height,
      ) else null
    )
  }
}