package com.example.foodist.utils

import android.content.res.Resources
import com.google.android.gms.maps.model.LatLng
import kotlin.math.cos
import kotlin.math.pow

data class LatLngDimensions(
  val startPointLat: Double,
  val endPointLat: Double,
  val startPointLng: Double,
  val endPointLng: Double,
)

class MapMeasurements {

  fun getRadius(latLng: LatLng, zoom: Double): Double {
    val width = getScreenWidth().toDouble()
    val perPixelSize = metersPerPx(latLng, zoom)
    return (width / 2) * perPixelSize
  }

  fun getLatLngDimensionsFromRadius(centerPoint: LatLng, radius: Double): LatLngDimensions {
    val degreeOffset = radius * METER_IN_DEGREES
    return LatLngDimensions(
      centerPoint.latitude - degreeOffset,
      centerPoint.latitude + degreeOffset,
      getUpdatedLongitude(centerPoint.latitude, centerPoint.longitude, -degreeOffset),
      getUpdatedLongitude(centerPoint.latitude, centerPoint.longitude, degreeOffset),
    )
  }

  private fun getUpdatedLongitude(lat: Double, lng: Double, degreeOffset: Double): Double {
    return lng + (degreeOffset) / cos(lat * (Math.PI / 180))
  }


  private fun metersPerPx(latLng: LatLng, zoom: Double): Double {
    return GOOGLE_MAPS_ZOOM_CONSTANT * cos(latLng.latitude * Math.PI / 180) / (2.0).pow(zoom + 1.0)
  }

  private fun getScreenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
  }

  private fun getScreenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
  }

  companion object {
    const val GOOGLE_MAPS_ZOOM_CONSTANT = 156543.03392
    const val METER_IN_DEGREES = (1 / ((2 * Math.PI / 360) * 6378.137)) / 1000

  }
}